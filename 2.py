"""
Excel 数据处理工具 - 命令行版
功能：处理Excel文件，合并相同商品的销量并输出结果
作者：YourName
版本：1.0
日期：2025-02-04
"""

#==================== 标准库导入 ====================

import os
from datetime import datetime
import threading

#==================== 延迟加载模块 ====================

def lazy_import_openpyxl():
    """
    后台线程加载openpyxl模块
    目的：提升程序启动速度，避免主线程阻塞
    """
    global load_workbook
    # 延迟导入大型库
    from openpyxl import load_workbook  # 用于操作Excel文件的核心库

==================== 主处理类 ====================

class ExcelProcessorApp:
    """Excel文件处理核心类"""

    def __init__(self):
        """初始化方法"""
        # 启动后台线程预加载openpyxl
        threading.Thread(target=lazy_import_openpyxl).start()

    def process_dropped_file(self, file_path):
        """
        处理文件入口方法
        参数：
            file_path (str): 用户输入的文件路径
        """
        # 路径预处理（兼容拖放路径格式）
        file_path = file_path.strip('"')  # 去除可能存在的双引号
        file_path = os.path.normpath(file_path)  # 统一路径格式（处理正反斜杠）

        # 路径有效性检查
        if not os.path.exists(file_path):
            print("[错误] 文件路径无效，请检查路径是否正确")
            return

        print(f"[系统] 开始处理文件：{os.path.basename(file_path)}")

        # 启动处理线程（避免界面卡顿）
        threading.Thread(target=self._process_excel, args=(file_path,)).start()

    def _process_excel(self, file_path):
        """
        Excel处理核心方法
        参数：
            file_path (str): 需要处理的Excel文件路径
        """
        try:
            # ---------- 文件加载 ----------
            from openpyxl import load_workbook  # 延迟加载

            # 注意：使用正常模式打开以便保存修改
            wb = load_workbook(file_path)  # 加载工作簿对象

            print("[系统] 文件加载成功，开始处理工作表...")

            # ---------- 工作表处理 ----------
            self._process_sales_sheet(wb)  # 处理销售表

            # ---------- 生成新文件名 ----------
            today = datetime.now()
            month = str(today.month).lstrip('0')  # 去除前导零（1月显示为1而不是01）
            day = str(today.day).lstrip('0')
            new_file_name = f"销量统计表{month}.{day}.xlsx"  # 按需求生成文件名
            new_file_path = os.path.join(
                os.path.dirname(file_path),  # 保持与原文件相同目录
                new_file_name
            )

            # ---------- 保存处理结果 ----------
            wb.save(new_file_path)  # 保存为新文件
            print(f"[成功] 文件已保存至：\n{new_file_path}")

        except PermissionError:
            print("[错误] 文件被占用，请关闭Excel后重试")
        except Exception as e:
            print(f"[错误] 处理失败：{str(e)}")

    def _process_sales_sheet(self, wb):
        """
        处理销售表逻辑
        操作步骤：
            1. 合并相同商品的销量
            2. 删除其他不必要的列
            3. 最终只保留商品名称和销量
        参数：
            wb (Workbook): openpyxl的工作簿对象
        """
        sales_ws = wb.worksheets[1]  # 获取第二个工作表

        # 用一个字典存储合并后的商品名和销量
        merged_data = defaultdict(int)

        # 遍历所有行
        for row in range(2, sales_ws.max_row + 1):  # 假设第1行是标题
            product_name = sales_ws[f'C{row}'].value
            sales = sales_ws[f'F{row}'].value

            if product_name is None or sales is None:
                continue  # 跳过空值

            # 对商品名进行标准化（芝士酸奶、零蔗糖酸奶）
            if '芝士酸奶' in product_name:
                product_name = '芝士酸奶'
            if '零蔗糖酸奶' in product_name:
                product_name = '零蔗糖酸奶'

            # 合并相同商品名的销量
            merged_data[product_name] += sales

        # 清空所有列，保留A列和B列
        for row in range(2, sales_ws.max_row + 1):
            for col in range(1, sales_ws.max_column + 1):
                sales_ws.cell(row=row, column=col).value = None

        # 将合并后的商品名和销量写回到A列和B列
        new_row = 2
        for product_name, total_sales in merged_data.items():
            sales_ws[f'A{new_row}'] = product_name
            sales_ws[f'B{new_row}'] = total_sales
            new_row += 1

#==================== 主程序入口 ====================

if __name__ == "__main__":
    # 初始化应用程序
    app = ExcelProcessorApp()

    # 用户交互界面  
    print("="*50)  
    print("Excel文件处理工具")  
    print("使用方法：")  
    print("1. 直接输入文件路径（如：C:\\文件.xlsx）")  
    print("2. 拖拽文件到本窗口自动获取路径")  
    print("3. 按Ctrl+C退出程序")  
    print("="*50)  

    while True:  
        try:  
            # 获取用户输入  
            file_path = input("\n请输入Excel文件路径：").strip()  

            # 处理退出命令  
            if file_path.lower() in ('exit', 'quit'):  
                break  

            # 处理空输入  
            if not file_path:  
                print("[提示] 输入不能为空，请重新输入")  
                continue  

            # 开始处理  
            app.process_dropped_file(file_path)  

        except KeyboardInterrupt:  
            print("\n[系统] 程序已退出")  
            break  
        except Exception as e:  
            print(f"[错误] 发生错误：{str(e)}")