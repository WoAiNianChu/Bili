import os
from datetime import datetime
import threading
from openpyxl import load_workbook


class ExcelProcessorApp:
    """Excel文件处理核心类"""

    def __init__(self):
        """初始化方法"""
        # 启动后台线程预加载openpyxl
        threading.Thread(target=self.lazy_import_openpyxl).start()

    def lazy_import_openpyxl(self):
        """
        后台线程加载openpyxl模块
        目的：提升程序启动速度，避免主线程阻塞
        """
        global load_workbook
        from openpyxl import load_workbook  # 用于操作Excel文件的核心库

    def process_files(self):
        """
        处理商品排行报表和产品统计表
        """
        # 获取文件路径
        ranking_file_path = input("请输入商品排行报表文件路径：").strip()
        product_file_path = input("请输入产品统计表文件路径：").strip()

        if not os.path.exists(ranking_file_path) or not os.path.exists(product_file_path):
            print("[错误] 文件路径无效，请检查路径是否正确")
            return

        # 处理商品排行报表
        print(f"正在处理商品排行报表：{ranking_file_path}")
        ranking_wb = load_workbook(ranking_file_path)
        ranking_ws = ranking_wb.active  # 获取第一个工作表
        product_sales = self.merge_product_sales(ranking_ws)

        # 处理产品统计表
        print(f"正在处理产品统计表：{product_file_path}")
        product_wb = load_workbook(product_file_path)
        product_ws = product_wb.active  # 获取第一个工作表

        # 将合并后的销量数据添加到产品统计表
        self.update_product_sales(product_ws, product_sales)

        # 保存处理后的产品统计表
        today = datetime.now()
        new_file_name = f"济南 产品统计表{today.month}.{today.day}.xlsx"
        new_file_path = os.path.join(os.path.dirname(product_file_path), new_file_name)
        product_wb.save(new_file_path)

        print(f"[成功] 文件已保存至：{new_file_path}")

    def merge_product_sales(self, ranking_ws):
        """
        合并商品排行报表中相似名称的商品销量
        :param ranking_ws: 商品排行报表工作表
        :return: 合并后的销量数据字典
        """
        product_sales = {}

        for row in range(2, ranking_ws.max_row + 1):  # 从第2行开始读取
            product_name = ranking_ws[f'C{row}'].value
            quantity = ranking_ws[f'F{row}'].value

            # 确保 quantity 是数字类型，如果是字符串，尝试转换为数字
            try:
                quantity = float(quantity)  # 将销量值转换为浮动类型
            except (ValueError, TypeError):
                quantity = 0  # 如果无法转换为数字，则设为0

            # 处理特殊合并规则
            if "草莓" in product_name and "鲜牛乳" in product_name:
                product_name = "草莓冷萃鲜牛乳"
            elif "开心果" in product_name and "鲜牛乳" in product_name:
                product_name = "开心果冷萃鲜牛乳"
            elif "抹茶" in product_name and "鲜牛乳" in product_name:
                product_name = "抹茶冷萃鲜牛乳"
            elif ("芋泥" in product_name or "香芋" in product_name) and "鲜牛乳" in product_name:
                product_name = "香芋冷萃鲜牛乳"
            elif "蔓越莓" in product_name:
                product_name = "蔓越莓胶原酸奶"
            elif "双蛋白" in product_name:
                product_name = "双蛋白酸奶"
            elif "零蔗糖" in product_name:
                product_name = "零蔗糖酸奶"
            elif "芝士" in product_name:
                product_name = "芝士酸奶"
            elif "紫米" in product_name:
                product_name = "紫米酸奶"
            elif "鲜牛奶" in product_name:
                product_name = "鲜牛奶"
            elif "液体酸奶" in product_name:
                product_name = "液体酸奶"
            elif "奶皮子" in product_name:
                product_name = "奶皮子酸奶酪"
            elif "布丁" in product_name:
                product_name = "布丁"
            elif "生巧" in product_name:
                product_name = "生巧可可牛奶"
            elif "香蕉" in product_name:
                product_name = "香蕉牛奶"
            elif "半口" in product_name:
                product_name = "半口奶酪"
            elif "罐罐" in product_name:
                product_name = "冷萃酸奶罐罐"
            elif "酸奶碗" in product_name:
                product_name = "酸奶碗—开心果能量"
     
            # 处理双皮奶合并规则：只合并果味双皮奶，不合并原味
            elif "双皮奶" in product_name and "原味" not in product_name:
                product_name = "果味双皮奶"

            # 累计销量
            if product_name not in product_sales:
                product_sales[product_name] = 0
            product_sales[product_name] += quantity

        return product_sales

    def update_product_sales(self, product_ws, product_sales):
        """
        将商品销量更新到产品统计表
        :param product_ws: 产品统计表工作表
        :param product_sales: 合并后的销量数据字典
        """
        for row in range(2, product_ws.max_row + 1):  # 从第2行开始读取
            product_name = product_ws[f'B{row}'].value  # 获取产品名称

            # 检查商品名称是否在合并后的销量数据中
            if product_name in product_sales:
                # 更新销量
                product_ws[f'D{row}'].value = product_sales[product_name]
                print(f"[更新] {product_name} - 销量: {product_sales[product_name]}")

# ==================== 主程序入口 ====================
if __name__ == "__main__":
    # 初始化应用程序
    app = ExcelProcessorApp()

    # 用户交互界面
    print("="*50)
    print("Excel文件处理工具")
    print("使用方法：")
    print("1. 请输入商品排行报表和产品统计表的文件路径")
    print("2. 按Ctrl+C退出程序")
    print("="*50)

    try:
        app.process_files()
    except KeyboardInterrupt:
        print("\n[系统] 程序已退出")
    except Exception as e:
        print(f"[错误] 发生未知错误：{str(e)}")
