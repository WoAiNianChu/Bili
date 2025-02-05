import os
import re
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
        """处理商品排行报表和产品统计表（只修改销售表）"""
        ranking_file_path = self.sanitize_path(input("请输入商品排行报表文件路径："))
        product_file_path = self.sanitize_path(input("请输入产品统计表文件路径："))

        if not os.path.exists(ranking_file_path) or not os.path.exists(product_file_path):
            print("[错误] 文件路径无效，请检查路径是否正确")
            input("按回车键退出...")
            return

        # 读取商品排行报表（只修改销售表）
        print(f"正在处理商品排行报表：{ranking_file_path}")
        ranking_wb = load_workbook(ranking_file_path)
        sales_ws = ranking_wb.worksheets[1]  # 只读取销售表

        # 读取产品统计表（只修改销售表）
        print(f"正在处理产品统计表：{product_file_path}")
        product_wb = load_workbook(product_file_path)
        product_ws = product_wb.worksheets[1]  # 只修改第二个表（销售数据）

        # 读取销售表并合并数据
        product_sales, e_sales = self.merge_product_sales(sales_ws)

        # 更新产品统计表的销售表
        self.update_product_sales(product_ws, product_sales, e_sales)

        # 保存处理后的产品统计表（不修改其他表）
        today = datetime.now()
        new_file_name = f"济南 产品统计表{today.month}-{today.day}.xlsx"
        new_file_path = os.path.join(os.path.dirname(product_file_path), new_file_name)
        product_wb.save(new_file_path)

        print(f"[成功] 文件已保存至：{new_file_path}")
        input("处理完成，按回车键退出...")

    def merge_product_sales(self, ranking_ws):
        """
        合并商品排行报表中相似名称的商品销量，并收集饿了么外卖和美团外卖的映射销量
        :param ranking_ws: 商品排行报表工作表
        :return: 合并后的销量数据字典，和分别映射到饿了么外卖和美团外卖的销量
        """
        product_sales = {}
        e_sales = {'饿了么外卖': {}, '美团外卖': {}}  # 用于存储饿了么外卖和美团外卖销量

        for row in range(2, ranking_ws.max_row + 1):  # 从第2行开始读取
            product_name = ranking_ws[f'C{row}'].value
            quantity = ranking_ws[f'F{row}'].value
            e_type = ranking_ws[f'E{row}'].value  # 获取E列（映射类型）

            # 确保 quantity 是数字类型，如果是字符串，尝试转换为数字
            try:
                quantity = float(quantity)  # 将销量值转换为浮点数
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
            elif "双皮奶" in product_name and "原味" not in product_name:
                product_name = "果味双皮奶"

            # 累计销量
            if product_name not in product_sales:
                product_sales[product_name] = 0
            product_sales[product_name] += quantity

            # 根据E列信息合并到对应的销售类型（饿了么外卖或美团外卖）
            if "未映射饿了么" in str(e_type):
                if product_name not in e_sales['饿了么外卖']:
                    e_sales['饿了么外卖'][product_name] = 0
                e_sales['饿了么外卖'][product_name] += quantity
            elif "未映射美团" in str(e_type):
                if product_name not in e_sales['美团外卖']:
                    e_sales['美团外卖'][product_name] = 0
                e_sales['美团外卖'][product_name] += quantity

        return product_sales, e_sales

    def update_product_sales(self, product_ws, product_sales, e_sales):
        """
        将商品销量更新到产品统计表
        :param product_ws: 产品统计表工作表
        :param product_sales: 合并后的销量数据字典
        :param e_sales: 各销售类型（饿了么外卖、美团外卖）映射销量
        """
        for row in range(2, product_ws.max_row + 1):  # 从第2行开始读取
            product_name = product_ws[f'B{row}'].value  # 获取产品名称

            # 更新销量
            if product_name in product_sales:
                product_ws[f'D{row}'].value = product_sales[product_name]

            # 更新饿了么外卖销量
            if product_name in e_sales['饿了么外卖']:
                product_ws[f'H{row}'].value = e_sales['饿了么外卖'][product_name]

            # 更新美团外卖销量
            if product_name in e_sales['美团外卖']:
                product_ws[f'G{row}'].value = e_sales['美团外卖'][product_name]

            # 只有当D列有值时才添加E列公式
            if product_ws[f'D{row}'].value is not None and product_ws[f'D{row}'].value != "":
                if 3 <= row <= 29:
                    product_ws[f'E{row}'].value = f"=D{row} - SUM(F{row}:I{row})"