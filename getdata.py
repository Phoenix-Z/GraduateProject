import random

attr = ['age', 'workclass', 'fnlwgt', 'education', 'education-num', 'marital-status', 'occupation',
        'relationship', 'race', 'sex', 'capital-gain', 'capital-loss', 'hours-per-week', 'native-country']
attr_map = {
    "workclass": ["Private", "Self-emp-not-inc", "Self-emp-inc", "Federal-gov", "Local-gov", "State-gov", "Without-pay",
                  "Never-worked"],
    "education": ["Bachelors", "Some-college", "11th", "HS-grad", "Prof-school", "Assoc-acdm", "Assoc-voc", '9th',
                  '7th-8th', '12th', 'Masters', '1st-4th', '10th', 'Doctorate', '5th-6th', 'Preschool'],
    'marital-status': ['Married-civ-spouse', 'Divorced', 'Never-married', 'Separated', 'Widowed',
                       'Married-spouse-absent', 'Married-AF-spouse'],
    'occupation': ['Tech-support', 'Craft-repair', 'Other-service', 'Sales', 'Exec-managerial', 'Prof-specialty',
                   'Handlers-cleaners', 'Machine-op-inspct', 'Adm-clerical', 'Farming-fishing', 'Transport-moving',
                   'Priv-house-serv', 'Protective-serv', 'Armed-Forces'],
    'relationship': ['Wife', 'Own-child', 'Husband', 'Not-in-family', 'Other-relative', 'Unmarried'],
    'race': ['White', 'Asian-Pac-Islander', 'Amer-Indian-Eskimo', 'Other', 'Black'],
    'sex': ['Female', 'Male'],
    'native-country': ['United-States', 'Cambodia', 'England', 'Puerto-Rico', 'Canada', 'Germany',
                       'Outlying-US(Guam-USVI-etc)', 'India', 'Japan', 'Greece', 'South', 'China', 'Cuba', 'Iran',
                       'Honduras', 'Philippines', 'Italy', 'Poland', 'Jamaica', 'Vietnam', 'Mexico', 'Portugal',
                       'Ireland', 'France', 'Dominican-Republic', 'Laos', 'Ecuador', 'Taiwan', 'Haiti', 'Columbia',
                       'Hungary', 'Guatemala', 'Nicaragua', 'Scotland', 'Thailand', 'Yugoslavia', 'El-Salvador',
                       'Trinadad&Tobago', 'Peru', 'Hong', 'Holand-Netherlands']
}


f2 = open('C:\Users\Phoenix-Z\Desktop\\adult2.txt', 'w')
with open('C:\Users\Phoenix-Z\Desktop\\adult.txt') as f:
    for line in f:
        words = line.split(',')
        new_words = []
        print line
        for index, word in enumerate(words):
            if index == 0:
                age = int(word.strip())
                if age <= 20:
                    new_words.append('<=20')
                elif 20 < age <= 30:
                    new_words.append('20<age<=30')
                elif 30 < age <= 40:
                    new_words.append('30<age<=40')
                elif 40 < age <= 50:
                    new_words.append('40<age<=50')
                else:
                    new_words.append('>50')
                continue
            if index in [2, 4, 10, 11, 12]:
                continue
            if word.strip() == '?':
                new_words.append(attr_map[attr[index]][random.randint(0, len(attr_map[attr[index]]) - 1)])
                continue
            new_words.append(word.strip())
        f2.write('\t'.join(new_words))
        f2.write('\n')

f2.close()

