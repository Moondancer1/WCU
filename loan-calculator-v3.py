#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""


import math
from datetime import datetime, timedelta

class LoanCalculator:
    def __init__(self):
        self.borrowers = []
        self.min_salary_usd = 500  # Минимальная зарплата в долларах
        self.usd_to_azn = 1.70     # Курс доллара к манату
        self.standard_terms = [3, 6, 9, 12, 24, 36]  # Стандартные сроки в месяцах
        self.min_rate = 11         # Минимальная ставка для длинных сроков
        self.max_rate = 14         # Максимальная ставка для коротких сроков
        self.max_payment_ratio = 10  # Максимальная кредитная нагрузка 10%
        
    def clear_screen(self):
        """Очистка экрана"""
        import os
        os.system('cls' if os.name == 'nt' else 'clear')
        
    def print_header(self, title):
        """Красивый заголовок"""
        print("=" * 75)
        print(f"  {title}")
        print("=" * 75)
        
    def calculate_interest_rate(self, months):
        """Расчет процентной ставки в зависимости от срока"""
        if months <= 3:
            return 14.0  # Максимальная ставка для 3 месяцев
        elif months >= 36:
            return 11.0  # Минимальная ставка для 36+ месяцев
        else:
            # Линейная интерполяция между 3 и 36 месяцами
            rate = self.max_rate - ((self.max_rate - self.min_rate) * (months - 3) / (36 - 3))
            return round(rate, 1)  # Округляем до 0.1%
    
    def calculate_max_loan_amount(self, monthly_income, annual_rate, loan_term_months):
        """Расчет максимальной суммы кредита при 10% нагрузке"""
        max_monthly_payment = monthly_income * (self.max_payment_ratio / 100)
        
        if annual_rate == 0:
            return max_monthly_payment * loan_term_months
        
        monthly_rate = annual_rate / 100 / 12
        max_loan = max_monthly_payment * ((1 + monthly_rate)**loan_term_months - 1) / \
                  (monthly_rate * (1 + monthly_rate)**loan_term_months)
        return max_loan
    
    def get_marital_status(self):
        """Ввод семейного положения"""
        print("\n💑 СЕМЕЙНОЕ ПОЛОЖЕНИЕ:")
        print("1. Холост/Не замужем")
        print("2. Женат/Замужем")
        print("3. Разведен/Разведена")
        print("4. Вдовец/Вдова")
        print("5. Гражданский брак")
        
        while True:
            try:
                choice = int(input("Выберите (1-5): "))
                if choice == 1:
                    return "Холост/Не замужем"
                elif choice == 2:
                    return "Женат/Замужем"
                elif choice == 3:
                    return "Разведен/Разведена"
                elif choice == 4:
                    return "Вдовец/Вдова"
                elif choice == 5:
                    return "Гражданский брак"
                else:
                    print("❌ Выберите вариант от 1 до 5!")
            except ValueError:
                print("❌ Введите корректное число!")
        
    def input_borrower_data(self):
        """Ввод данных заемщика с возможностью повтора"""
        print("\n📝 ВВОД ДАННЫХ ЗАЕМЩИКА")
        print("-" * 35)
        
        first_name = input("Введите имя: ").strip()
        while not first_name:
            print("❌ Имя не может быть пустым!")
            first_name = input("Введите имя: ").strip()
            
        last_name = input("Введите фамилию: ").strip()
        while not last_name:
            print("❌ Фамилия не может быть пустой!")
            last_name = input("Введите фамилию: ").strip()
        
        # Семейное положение
        marital_status = self.get_marital_status()
        
        # Ввод зарплаты с проверкой и возможностью повтора
        while True:
            try:
                salary_input = input("\nВведите зарплату (в долларах США): $")
                salary_usd = float(salary_input)
                
                if salary_usd < 0:
                    print("❌ Зарплата не может быть отрицательной!")
                    continue
                
                if salary_usd < self.min_salary_usd:
                    print(f"\n❌ НЕДОСТАТОЧНЫЙ ДОХОД!")
                    print(f"   Минимальная зарплата: ${self.min_salary_usd}")
                    print(f"   Ваша зарплата: ${salary_usd}")
                    print(f"   Недостает: ${self.min_salary_usd - salary_usd}")
                    print("\nВарианты:")
                    print("1. Ввести зарплату заново")
                    print("2. Добавить еще одного заемщика позже")
                    print("3. Выйти из программы")
                    
                    choice = input("\nВыберите действие (1/2/3): ").strip()
                    
                    if choice == "1":
                        continue  # Повторный ввод зарплаты
                    elif choice == "2":
                        print("⚠️ Запомните: общий доход всех заемщиков должен быть достаточным!")
                        break  # Принимаем этого заемщика, но с предупреждением
                    elif choice == "3":
                        print("\n👋 Программа завершена. До свидания!")
                        return None
                    else:
                        print("❌ Неверный выбор, попробуйте еще раз")
                        continue
                
                break  # Зарплата прошла проверку
                
            except ValueError:
                print("❌ Введите корректную сумму (только цифры)!")
        
        # Ввод количества членов семьи
        while True:
            try:
                family_members = int(input("Количество членов семьи: "))
                if family_members < 1:
                    print("❌ Количество членов семьи должно быть больше 0!")
                    continue
                if family_members > 20:
                    print("❌ Слишком большая семья! Максимум 20 человек.")
                    continue
                break
            except ValueError:
                print("❌ Введите корректное число!")
        
        # Конвертация в манаты
        salary_azn = salary_usd * self.usd_to_azn
        
        borrower = {
            'first_name': first_name,
            'last_name': last_name,
            'marital_status': marital_status,
            'salary_usd': salary_usd,
            'salary_azn': salary_azn,
            'family_members': family_members,
            'full_name': f"{first_name} {last_name}"
        }
        
        self.borrowers.append(borrower)
        
        print(f"\n✅ Заемщик добавлен:")
        print(f"   👤 {borrower['full_name']}")
        print(f"   💑 Семейное положение: {marital_status}")
        print(f"   💵 Зарплата: ${salary_usd} (≈{salary_azn:,.0f} AZN)")
        print(f"   👨‍👩‍👧‍👦 Семья: {family_members} человек")
        
        return borrower
    
    def check_total_income_requirement(self):
        """Проверка общего дохода всех заемщиков"""
        total_usd = sum(b['salary_usd'] for b in self.borrowers)
        total_azn = sum(b['salary_azn'] for b in self.borrowers)
        
        if total_usd < self.min_salary_usd:
            print(f"\n❌ ОТКАЗ В КРЕДИТЕ!")
            print(f"   Общий доход всех заемщиков: ${total_usd:.2f} (≈{total_azn:.0f} AZN)")
            print(f"   Минимальный требуемый доход: ${self.min_salary_usd}")
            print(f"   Недостает: ${self.min_salary_usd - total_usd:.2f}")
            print("\n💡 Рекомендации:")
            print("   - Добавьте еще одного заемщика")
            print("   - Увеличьте доходы существующих заемщиков")
            print("   - Обратитесь в банк для индивидуального рассмотрения")
            
            input("\nНажмите Enter для завершения программы...")
            return False
        
        return True
    
    def show_max_loan_recommendations(self):
        """Показать рекомендации по максимальной сумме кредита"""
        total_income = self.calculate_total_income()
        max_monthly_payment = total_income * (self.max_payment_ratio / 100)
        
        print(f"\n💡 РЕКОМЕНДАЦИИ ПО МАКСИМАЛЬНОЙ СУММЕ КРЕДИТА:")
        print(f"   Ваш доход: {total_income:,.0f} AZN/месяц")
        print(f"   Максимальный платеж (10%): {max_monthly_payment:,.0f} AZN/месяц")
        print("-" * 60)
        
        for months in self.standard_terms:
            rate = self.calculate_interest_rate(months)
            max_loan = self.calculate_max_loan_amount(total_income, rate, months)
            years = months / 12
            period_text = f"{months} мес." if months < 12 else f"{months} мес. ({years:.0f}г.)"
            print(f"   {period_text:>12} ({rate:>4.1f}%): до {max_loan:>8,.0f} AZN")
    
    def print_rate_table(self):
        """Показать таблицу процентных ставок"""
        print("\n📊 ТАБЛИЦА ПРОЦЕНТНЫХ СТАВОК:")
        print("-" * 45)
        print(f"{'Срок':>10} {'Ставка':>12} {'Описание'}")
        print("-" * 45)
        
        for months in self.standard_terms:
            rate = self.calculate_interest_rate(months)
            years = months / 12
            if months < 12:
                period_text = f"{months} мес."
            else:
                period_text = f"{months} мес." if years != int(years) else f"{int(years)} года"
            
            print(f"{period_text:>10} {rate:>10.1f}% {'Короткий срок' if months <= 6 else 'Средний срок' if months <= 18 else 'Длинный срок'}")
        
        print(f"{'Свой срок':>10} {'11-14%':>12} Зависит от срока")
        print("-" * 45)
        print("💡 Чем больше срок, тем меньше процентная ставка")
    
    def get_loan_term(self):
        """Выбор срока кредита"""
        print(f"\n📅 ВЫБОР СРОКА КРЕДИТА:")
        
        # Показать таблицу ставок и рекомендации
        self.print_rate_table()
        self.show_max_loan_recommendations()
        
        print("\nВарианты:")
        for i, term in enumerate(self.standard_terms, 1):
            rate = self.calculate_interest_rate(term)
            years = term / 12
            if term < 12:
                period_text = f"{term} месяцев"
            else:
                period_text = f"{term} месяцев ({years:.1f} года)" if years != int(years) else f"{term} месяцев ({int(years)} года)"
            print(f"{i}. {period_text} - {rate}% годовых")
        
        print(f"{len(self.standard_terms) + 1}. Указать свой срок (1-60 месяцев)")
        
        while True:
            try:
                choice = int(input(f"\nВыберите вариант (1-{len(self.standard_terms) + 1}): "))
                
                if 1 <= choice <= len(self.standard_terms):
                    selected_term = self.standard_terms[choice - 1]
                    selected_rate = self.calculate_interest_rate(selected_term)
                    print(f"✅ Выбран срок: {selected_term} месяцев")
                    print(f"✅ Процентная ставка: {selected_rate}% годовых")
                    return selected_term, selected_rate
                
                elif choice == len(self.standard_terms) + 1:
                    # Пользовательский срок
                    while True:
                        try:
                            custom_months = int(input("Введите срок в месяцах (1-60): "))
                            if custom_months < 1 or custom_months > 60:
                                print("❌ Срок должен быть от 1 до 60 месяцев!")
                                continue
                            
                            custom_rate = self.calculate_interest_rate(custom_months)
                            print(f"✅ Выбран срок: {custom_months} месяцев")
                            print(f"✅ Процентная ставка: {custom_rate}% годовых")
                            return custom_months, custom_rate
                        except ValueError:
                            print("❌ Введите корректное число месяцев!")
                
                else:
                    print(f"❌ Выберите вариант от 1 до {len(self.standard_terms) + 1}!")
                    
            except ValueError:
                print("❌ Введите корректное число!")
    
    def input_loan_parameters(self):
        """Ввод параметров кредита"""
        print("\n💰 ПАРАМЕТРЫ КРЕДИТА")
        print("-" * 30)
        
        # Выбор срока кредита сначала для показа рекомендаций
        loan_term_months, annual_rate = self.get_loan_term()
        
        # Расчет максимально допустимой суммы
        total_income = self.calculate_total_income()
        max_loan_amount = self.calculate_max_loan_amount(total_income, annual_rate, loan_term_months)
        
        print(f"\n💡 При выбранном сроке ({loan_term_months} мес., {annual_rate}%):")
        print(f"   Максимальная сумма кредита: {max_loan_amount:,.0f} AZN")
        print(f"   Максимальный платеж: {total_income * 0.1:,.0f} AZN/месяц (10% от дохода)")
        
        # Сумма кредита
        while True:
            try:
                loan_amount = float(input(f"\nВведите желаемую сумму кредита (не более {max_loan_amount:,.0f} AZN): "))
                if loan_amount <= 0:
                    print("❌ Сумма должна быть больше 0!")
                    continue
                if loan_amount > max_loan_amount:
                    print(f"\n❌ ОТКАЗ В КРЕДИТЕ!")
                    print(f"   Запрашиваемая сумма: {loan_amount:,.0f} AZN")
                    print(f"   Максимально допустимая: {max_loan_amount:,.0f} AZN")
                    print(f"   Превышение: {loan_amount - max_loan_amount:,.0f} AZN")
                    print(f"\n💡 При данной сумме кредитная нагрузка превысит 10%!")
                    
                    print("\nВарианты:")
                    print("1. Ввести сумму заново")
                    print("2. Выбрать другой срок кредита")
                    print("3. Завершить программу")
                    
                    choice = input("\nВыберите действие (1/2/3): ").strip()
                    if choice == "1":
                        continue
                    elif choice == "2":
                        # Возвращаемся к выбору срока
                        loan_term_months, annual_rate = self.get_loan_term()
                        max_loan_amount = self.calculate_max_loan_amount(total_income, annual_rate, loan_term_months)
                        print(f"\n💡 При новом сроке ({loan_term_months} мес., {annual_rate}%):")
                        print(f"   Максимальная сумма кредита: {max_loan_amount:,.0f} AZN")
                        continue
                    elif choice == "3":
                        print("\n👋 Программа завершена. До свидания!")
                        return None
                    else:
                        print("❌ Неверный выбор!")
                        continue
                
                break
            except ValueError:
                print("❌ Введите корректную сумму!")
        
        return {
            'amount': loan_amount,
            'rate': annual_rate,
            'months': loan_term_months,
            'years': loan_term_months / 12,
            'max_allowed': max_loan_amount
        }
    
    def calculate_total_income(self):
        """Общий доход в манатах"""
        return sum(borrower['salary_azn'] for borrower in self.borrowers)
    
    def calculate_monthly_payment(self, loan_amount, annual_rate, loan_term_months):
        """Расчет ежемесячного платежа"""
        if annual_rate == 0:
            return loan_amount / loan_term_months
        
        monthly_rate = annual_rate / 100 / 12
        payment = loan_amount * (monthly_rate * (1 + monthly_rate)**loan_term_months) / \
                 ((1 + monthly_rate)**loan_term_months - 1)
        return payment
    
    def generate_payment_schedule(self, loan_amount, annual_rate, loan_term_months):
        """Создание графика платежей"""
        monthly_payment = self.calculate_monthly_payment(loan_amount, annual_rate, loan_term_months)
        monthly_rate = annual_rate / 100 / 12
        
        schedule = []
        remaining_balance = loan_amount
        start_date = datetime.now()
        
        for month in range(1, loan_term_months + 1):
            payment_date = start_date + timedelta(days=30 * (month - 1))
            
            interest_payment = remaining_balance * monthly_rate
            principal_payment = monthly_payment - interest_payment
            remaining_balance -= principal_payment
            
            # Корректировка последнего платежа
            if month == loan_term_months and remaining_balance != 0:
                principal_payment += remaining_balance
                monthly_payment = interest_payment + principal_payment
                remaining_balance = 0
            
            schedule.append({
                'month': month,
                'date': payment_date.strftime('%Y-%m-%d'),
                'monthly_payment': round(monthly_payment, 2),
                'principal': round(principal_payment, 2),
                'interest': round(interest_payment, 2),
                'balance': round(max(0, remaining_balance), 2)
            })
        
        return schedule
    
    def analyze_affordability(self, monthly_payment):
        """Анализ доступности кредита"""
        total_income = self.calculate_total_income()
        if total_income == 0:
            return None
        
        payment_ratio = (monthly_payment / total_income) * 100
        
        # При новой логике все одобренные кредиты должны быть в пределах 10%
        married_count = sum(1 for b in self.borrowers if "Женат" in b['marital_status'] or "Замужем" in b['marital_status'])
        family_bonus = " Семейный статус обеспечивает дополнительную стабильность." if married_count > 0 else ""
        
        status = "✅ КРЕДИТ ОДОБРЕН"
        recommendation = f"Кредитная нагрузка {payment_ratio:.1f}% - в пределах допустимого лимита 10%.{family_bonus}"
        
        return {
            'total_income': total_income,
            'monthly_payment': monthly_payment,
            'payment_ratio': payment_ratio,
            'remaining_income': total_income - monthly_payment,
            'status': status,
            'recommendation': recommendation
        }
    
    def print_borrowers_analysis(self, monthly_payment):
        """Анализ нагрузки по заемщикам"""
        total_income = self.calculate_total_income()
        
        print("\n👥 АНАЛИЗ НАГРУЗКИ ПО ЗАЕМЩИКАМ:")
        print("-" * 65)
        
        for i, borrower in enumerate(self.borrowers, 1):
            share = borrower['salary_azn'] / total_income
            borrower_payment = monthly_payment * share
            borrower_ratio = (borrower_payment / borrower['salary_azn']) * 100
            remaining = borrower['salary_azn'] - borrower_payment
            
            print(f"\n{i}. {borrower['full_name']}")
            print(f"   💑 Семейное положение: {borrower['marital_status']}")
            print(f"   💵 Зарплата: ${borrower['salary_usd']} ({borrower['salary_azn']:,.0f} AZN)")
            print(f"   📊 Доля в семейном доходе: {share*100:.1f}%")
            print(f"   💳 Доля кредитного платежа: {borrower_payment:,.2f} AZN")
            print(f"   📈 Нагрузка на доход: {borrower_ratio:.1f}%")
            print(f"   💰 Остаток после платежа: {remaining:,.2f} AZN (90%+ дохода)")
    
    def print_payment_table(self, schedule, start_month=1, count=12):
        """Вывод таблицы платежей"""
        end_month = min(start_month + count - 1, len(schedule))
        
        print(f"\n📅 ГРАФИК ПЛАТЕЖЕЙ (месяцы {start_month}-{end_month}):")
        print("-" * 85)
        print(f"{'№':>3} {'Дата':>12} {'Платеж (AZN)':>15} {'Основной долг':>15} {'Проценты':>12} {'Остаток':>15}")
        print("-" * 85)
        
        for i in range(start_month-1, end_month):
            payment = schedule[i]
            print(f"{payment['month']:>3} "
                  f"{payment['date']:>12} "
                  f"{payment['monthly_payment']:>15,.0f} "
                  f"{payment['principal']:>15,.0f} "
                  f"{payment['interest']:>12,.0f} "
                  f"{payment['balance']:>15,.0f}")
    
    def print_family_summary(self):
        """Краткая информация о семейном положении"""
        print("\n👨‍👩‍👧‍👦 СЕМЕЙНАЯ ИНФОРМАЦИЯ:")
        print("-" * 40)
        
        total_family_members = sum(b['family_members'] for b in self.borrowers)
        married_count = sum(1 for b in self.borrowers if "Женат" in b['marital_status'] or "Замужем" in b['marital_status'])
        
        print(f"Общее количество членов семьи: {total_family_members}")
        print(f"Заемщиков в браке: {married_count} из {len(self.borrowers)}")
        
        if married_count > 0:
            print("✅ Семейный статус повышает кредитную надежность")
        
        # Статистика по семейному положению
        status_count = {}
        for borrower in self.borrowers:
            status = borrower['marital_status']
            status_count[status] = status_count.get(status, 0) + 1
        
        print("\nРаспределение по семейному положению:")
        for status, count in status_count.items():
            print(f"- {status}: {count} чел.")
    
    def print_financial_safety(self, analysis):
        """Показать информацию о финансовой безопасности"""
        print(f"\n💰 ФИНАНСОВАЯ БЕЗОПАСНОСТЬ:")
        print("-" * 50)
        remaining_percentage = 100 - analysis['payment_ratio']
        print(f"Остается от дохода: {remaining_percentage:.1f}% ({analysis['remaining_income']:,.0f} AZN)")
        print(f"Кредитная нагрузка: {analysis['payment_ratio']:.1f}% (лимит: {self.max_payment_ratio}%)")
        print(f"Запас до лимита: {self.max_payment_ratio - analysis['payment_ratio']:.1f}%")
        
        if remaining_percentage >= 90:
            print("✅ Отличная финансовая устойчивость")
        elif remaining_percentage >= 85:
            print("✅ Хорошая финансовая устойчивость")
        else:
            print("⚠️ Умеренная финансовая устойчивость")
    
    def run(self):
        """Основная функция программы"""
        try:
            self.clear_screen()
            self.print_header("КРЕДИТНЫЙ КАЛЬКУЛЯТОР (АЗЕРБАЙДЖАН) v3.0")
            
            print(f"\n💰 Минимальная зарплата: ${self.min_salary_usd}")
            print(f"💱 Курс доллара: {self.usd_to_azn} AZN")
            print(f"📈 Процентные ставки: {self.min_rate}% - {self.max_rate}%")
            print(f"🚨 Максимальная кредитная нагрузка: {self.max_payment_ratio}%")
            print(f"🏦 Расчеты производятся в манатах (AZN)")
            print(f"\n💡 Принцип работы: Только 10% дохода на кредит, 90% остается вам!")
            
            # Ввод количества заемщиков
            while True:
                try:
                    num_borrowers = int(input(f"\nСколько заемщиков будет? (1-5): "))
                    if num_borrowers < 1 or num_borrowers > 5:
                        print("❌ Количество заемщиков должно быть от 1 до 5!")
                        continue
                    break
                except ValueError:
                    print("❌ Введите корректное число!")
            
            # Ввод данных заемщиков
            for i in range(num_borrowers):
                print(f"\n{'='*20} ЗАЕМЩИК {i+1} {'='*20}")
                borrower = self.input_borrower_data()
                if borrower is None:
                    return  # Пользователь выбрал выход
            
            # Проверка общего дохода
            if not self.check_total_income_requirement():
                return
            
            # Ввод параметров кредита
            loan_params = self.input_loan_parameters()
            if loan_params is None:
                return  # Пользователь завершил программу
            
            # Расчеты
            monthly_payment = self.calculate_monthly_payment(
                loan_params['amount'], 
                loan_params['rate'], 
                loan_params['months']
            )
            
            schedule = self.generate_payment_schedule(
                loan_params['amount'],
                loan_params['rate'], 
                loan_params['months']
            )
            
            analysis = self.analyze_affordability(monthly_payment)
            
            # Результаты
            self.clear_screen()
            self.print_header("КРЕДИТ ОДОБРЕН! РЕЗУЛЬТАТЫ РАСЧЕТА")
            
            print(f"\n🎉 ПОЗДРАВЛЯЕМ! ВАШ КРЕДИТ ОДОБРЕН!")
            print(f"💰 ПАРАМЕТРЫ КРЕДИТА:")
            print(f"   Сумма: {loan_params['amount']:,.0f} AZN")
            print(f"   Ставка: {loan_params['rate']}% годовых")
            print(f"   Срок: {loan_params['months']} месяцев ({loan_params['years']:.1f} года)")
            print(f"   Максимально было доступно: {loan_params['max_allowed']:,.0f} AZN")
            
            print(f"\n📊 ФИНАНСОВЫЙ АНАЛИЗ:")
            print(f"   Общий доход семьи: {analysis['total_income']:,.0f} AZN/месяц")
            print(f"   Ежемесячный платеж: {analysis['monthly_payment']:,.2f} AZN")
            print(f"   Кредитная нагрузка: {analysis['payment_ratio']:.1f}% (лимит: {self.max_payment_ratio}%)")
            print(f"   Остается свободных средств: {analysis['remaining_income']:,.2f} AZN ({100-analysis['payment_ratio']:.1f}%)")
            print(f"   Статус: {analysis['status']}")
            print(f"   💡 {analysis['recommendation']}")
            
            # Финансовая безопасность
            self.print_financial_safety(analysis)
            
            # Семейная информация
            self.print_family_summary()
            
            # Анализ по заемщикам
            self.print_borrowers_analysis(monthly_payment)
            
            # Таблицы платежей
            if loan_params['months'] <= 12:
                self.print_payment_table(schedule, 1, loan_params['months'])  # Все месяцы
            else:
                self.print_payment_table(schedule, 1, 12)  # Первые 12 месяцев
                if loan_params['months'] > 24:
                    self.print_payment_table(schedule, loan_params['months']-11, 12)  # Последние 12 месяцев
            
            # Итоговая статистика
            total_payments = sum(p['monthly_payment'] for p in schedule)
            total_interest = sum(p['interest'] for p in schedule)
            
            print(f"\n📈 ИТОГОВАЯ СТАТИСТИКА:")
            print("-" * 55)
            print(f"Общая сумма выплат: {total_payments:,.2f} AZN")
            print(f"Переплата по процентам: {total_interest:,.2f} AZN")
            print(f"Коэффициент переплаты: {(total_payments/loan_params['amount']-1)*100:.1f}%")
            print(f"Общая экономия семьи за срок кредита: {analysis['remaining_income'] * loan_params['months']:,.0f} AZN")
            
            # Сохранение в файл
            try:
                with open('loan_approved_azn_v3.txt', 'w', encoding='utf-8') as f:
                    f.write("ОДОБРЕННЫЙ КРЕДИТ - ГРАФИК ПЛАТЕЖЕЙ (АЗЕРБАЙДЖАН) v3.0\n")
                    f.write("=" * 65 + "\n\n")
                    f.write(f"СТАТУС: КРЕДИТ ОДОБРЕН!\n")
                    f.write(f"Максимальная кредитная нагрузка: {self.max_payment_ratio}%\n\n")
                    
                    f.write(f"Заемщики:\n")
                    for borrower in self.borrowers:
                        f.write(f"- {borrower['full_name']}: ${borrower['salary_usd']} ({borrower['salary_azn']:,.0f} AZN)\n")
                        f.write(f"  Семейное положение: {borrower['marital_status']}\n")
                        f.write(f"  Семья: {borrower['family_members']} человек\n\n")
                    
                    f.write(f"Параметры кредита:\n")
                    f.write(f"- Сумма: {loan_params['amount']:,.0f} AZN\n")
                    f.write(f"- Ставка: {loan_params['rate']}% (зависит от срока)\n")
                    f.write(f"- Срок: {loan_params['months']} месяцев\n")
                    f.write(f"- Ежемесячный платеж: {monthly_payment:,.2f} AZN\n")
                    f.write(f"- Кредитная нагрузка: {analysis['payment_ratio']:.1f}%\n\n")
                    
                    f.write("МЕСЯЦ\tДАТА\t\tПЛАТЕЖ (AZN)\tОСНОВНОЙ ДОЛГ\tПРОЦЕНТЫ\tОСТАТОК\n")
                    f.write("-" * 80 + "\n")
                    for payment in schedule:
                        f.write(f"{payment['month']}\t{payment['date']}\t{payment['monthly_payment']:.2f}\t\t"
                               f"{payment['principal']:.2f}\t{payment['interest']:.2f}\t{payment['balance']:.2f}\n")
                
                print(f"\n✅ График сохранен в файл: loan_approved_azn_v3.txt")
            except Exception as e:
                print(f"\n⚠️ Ошибка сохранения файла: {e}")
            
            print(f"\n🎉 КРЕДИТ УСПЕШНО ОДОБРЕН И РАССЧИТАН!")
            print(f"💡 Помните: у вас остается {100-analysis['payment_ratio']:.1f}% дохода для других расходов!")
            input("\nНажмите Enter для выхода...")
            
        except KeyboardInterrupt:
            print("\n\n👋 Программа прервана пользователем. До свидания!")
        except Exception as e:
            print(f"\n❌ Произошла ошибка: {e}")
            print("Попробуйте запустить программу заново.")
            input("\nНажмите Enter для выхода...")

# Запуск программы
if __name__ == "__main__":
    calculator = LoanCalculator()
    calculator.run()
