import os
import xml.etree.ElementTree as ET

res_dir = 'app/src/main/res/layout'
ns = {'android': 'http://schemas.android.com/apk/res/android', 'app': 'http://schemas.android.com/apk/res-auto'}

for root_dir, _, files in os.walk(res_dir):
    for f in files:
        if not f.endswith('.xml'): continue
        path = os.path.join(root_dir, f)
        try:
            tree = ET.parse(path)
            root_element = tree.getroot()
            
            # Find all ConstraintLayouts
            for cl in root_element.iter():
                # We can check tag names ending with ConstraintLayout
                if 'ConstraintLayout' in cl.tag:
                    for child in cl:
                        if 'Guideline' in child.tag or 'Barrier' in child.tag:
                            continue
                        
                        has_horiz = False
                        has_vert = False
                        
                        for key in child.keys():
                            if 'layout_constraintStart' in key or 'layout_constraintEnd' in key or 'layout_constraintLeft' in key or 'layout_constraintRight' in key:
                                has_horiz = True
                            if 'layout_constraintTop' in key or 'layout_constraintBottom' in key or 'layout_constraintBaseline' in key:
                                has_vert = True
                        
                        if not has_horiz or not has_vert:
                            print(f"{path}: Element {child.tag} missing constraints (horiz={has_horiz}, vert={has_vert})")
                            # print(child.attrib)
        except Exception as e:
            pass
