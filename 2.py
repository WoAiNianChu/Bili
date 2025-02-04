"""
Excel 数据处理工具 - 商品名称合并版
功能：通过命令行接收Excel文件路径，处理后生成带日期的副本文件
作者：YourName
版本：1.0
日期：2023-10-05
"""

# ==================== 标准库导入 ====================
import os
from datetime import datetime
import threading
import pandas as pd

# ==================== 主处理类 ====================
class ExcelProcessorApp:
    """Excel文件处理核心类"""
    
    def __init__(self):
        """初始化方法"""
        pass

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
            df = pd.read_excel(file_path)  # 加载DataFrame
            
            print("[系统] 文件加载成功，开始处理数据...")

            # ---------- 数据处理 ----------
            self._process_data(df)  # 处理数据

            # ---------- 生成新文件名 ----------
            today = datetime.now()
            month = str(today.month).lstrip('0')  # 去除前导零（1月显示为1而不是01）
            day = str(today.day).lstrip('0')
            new_file_name = f"处理后商品统计表_{month}.{day}.xlsx"  # 按需求生成文件名
            new_file_path = os.path.join(
                os.path.dirname(file_path),  # 保持与原文件相同目录
                new_file_name
            )

            # ---------- 保存处理结果 ----------
            df.to_excel(new_file_path, index=False)  # 保存为新文件
            print(f"[成功] 文件已保存至：\n{new_file_path}")

        except PermissionError:
            print("[错误] 文件被占用，请关闭Excel后重试")
        except Exception as e:
            print(f"[错误] 处理失败：{str(e)}")

    def _process_data(self, df):
        """
        数据处理逻辑
        操作步骤：
            1. 合并特定名称的商品
            2. 将销量相加
            3. 删除不需要的列
        参数：
            df (DataFrame): pandas的DataFrame对象
        """
        # 定义一个函数，用于处理名称中的特定情况
        def process_name(name):
            if '芝士酸奶' in name:
                return '芝士酸奶'
            elif '零蔗糖酸奶' in name:
                return '零蔗糖酸奶'
            else:
                return name

        # 替换名称中的特定情况
        df['名称'] = df['名称'].apply(process_name)

        # 按商品名称合并销量
        result_df = df.groupby('名称')['销量'].sum().reset_index()

        # 选择需要的列
        df = result_df[['名称', '销量']]

# ==================== 主程序入口 ====================
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
            print(f"[错误] 发生异常：{str(e)}")
