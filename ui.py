import uiautomation as auto

def dump_controls(control, depth=0):
    indent = '  ' * depth
    control_type = auto.ControlTypeNames[control.ControlType]
    automation_id = control.AutomationId if control.AutomationId else "N/A"
    name = control.Name if control.Name else "N/A"
    
    # 获取数值（根据控件类型）
    value = ""
    if control.ControlType in [auto.ControlType.Edit, auto.ControlType.Document]:
        try:
            value = control.GetValuePattern().Value
        except:
            pass
    elif control.ControlType == auto.ControlType.Text:
        value = control.Name  # 文本控件的内容通常在 Name 属性

    print(f"{indent}[{control_type}] ID: {automation_id}, Value: {value}")

    # 递归遍历子控件
    for child in control.GetChildren():
        dump_controls(child, depth + 1)

# 定位目标窗口
window = auto.WindowControl(Name="饿了么商家版")
dump_controls(window)