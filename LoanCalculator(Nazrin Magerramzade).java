import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

/**
 * Loan Calculator - Кредитный калькулятор (Азербайджан) v3.0 Java Edition
 * Интерактивная программа для расчета кредитов с ограничением нагрузки
 * 
 * Возможности:
 * - Максимальная кредитная нагрузка: 10% от дохода
 * - Автоматический отказ при превышении лимита
 * - Расчет рекомендуемой суммы кредита
 * - Учет семейного положения
 * - Вариативная процентная ставка (11-14% в зависимости от срока)
 */
public class LoanCalculator {
    
    // Константы
    private static final int MIN_SALARY_USD = 500;
    private static final double USD_TO_AZN = 1.70;
    private static final int[] STANDARD_TERMS = {3, 6, 9, 12, 24, 36};
    private static final double MIN_RATE = 11.0;
    private static final double MAX_RATE = 14.0;
    private static final double MAX_PAYMENT_RATIO = 10.0;
    
    // Поля класса
    private List<Borrower> borrowers;
    private Scanner scanner;
    private DecimalFormat currencyFormat;
    private DecimalFormat percentFormat;
    
    // Класс для заемщика
    static class Borrower {
        public String firstName;
        public String lastName;
        public String maritalStatus;
        public double salaryUsd;
        public double salaryAzn;
        public int familyMembers;
        public String fullName;
        
        public Borrower(String firstName, String lastName, String maritalStatus, 
                       double salaryUsd, int familyMembers) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.maritalStatus = maritalStatus;
            this.salaryUsd = salaryUsd;
            this.salaryAzn = salaryUsd * USD_TO_AZN;
            this.familyMembers = familyMembers;
            this.fullName = firstName + " " + lastName;
        }
    }
    
    // Класс для параметров кредита
    static class LoanParams {
        public double amount;
        public double rate;
        public int months;
        public double years;
        public double maxAllowed;
        
        public LoanParams(double amount, double rate, int months, double maxAllowed) {
            this.amount = amount;
            this.rate = rate;
            this.months = months;
            this.years = months / 12.0;
            this.maxAllowed = maxAllowed;
        }
    }
    
    // Класс для платежа
    static class Payment {
        public int month;
        public String date;
        public double monthlyPayment;
        public double principal;
        public double interest;
        public double balance;
        
        public Payment(int month, String date, double monthlyPayment, 
                      double principal, double interest, double balance) {
            this.month = month;
            this.date = date;
            this.monthlyPayment = monthlyPayment;
            this.principal = principal;
            this.interest = interest;
            this.balance = balance;
        }
    }
    
    // Класс для анализа доступности
    static class AffordabilityAnalysis {
        public double totalIncome;
        public double monthlyPayment;
        public double paymentRatio;
        public double remainingIncome;
        public String status;
        public String recommendation;
        
        public AffordabilityAnalysis(double totalIncome, double monthlyPayment, 
                                   double paymentRatio, double remainingIncome, 
                                   String status, String recommendation) {
            this.totalIncome = totalIncome;
            this.monthlyPayment = monthlyPayment;
            this.paymentRatio = paymentRatio;
            this.remainingIncome = remainingIncome;
            this.status = status;
            this.recommendation = recommendation;
        }
    }
    
    // Конструктор
    public LoanCalculator() {
        this.borrowers = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.currencyFormat = new DecimalFormat("#,##0.00");
        this.percentFormat = new DecimalFormat("0.0");
    }
    
    // Очистка экрана
    private void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Если очистка не удалась, просто выводим пустые строки
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    // Красивый заголовок
    private void printHeader(String title) {
        System.out.println("=".repeat(75));
        System.out.println("  " + title);
        System.out.println("=".repeat(75));
    }
    
    // Расчет процентной ставки в зависимости от срока
    private double calculateInterestRate(int months) {
        if (months <= 3) {
            return MAX_RATE; // 14.0% для 3 месяцев
        } else if (months >= 36) {
            return MIN_RATE; // 11.0% для 36+ месяцев
        } else {
            // Линейная интерполяция между 3 и 36 месяцами
            double rate = MAX_RATE - ((MAX_RATE - MIN_RATE) * (months - 3) / (36 - 3));
            return Math.round(rate * 10.0) / 10.0; // Округляем до 0.1%
        }
    }
    
    // Расчет максимальной суммы кредита при 10% нагрузке
    private double calculateMaxLoanAmount(double monthlyIncome, double annualRate, int loanTermMonths) {
        double maxMonthlyPayment = monthlyIncome * (MAX_PAYMENT_RATIO / 100);
        
        if (annualRate == 0) {
            return maxMonthlyPayment * loanTermMonths;
        }
        
        double monthlyRate = annualRate / 100 / 12;
        double maxLoan = maxMonthlyPayment * (Math.pow(1 + monthlyRate, loanTermMonths) - 1) /
                        (monthlyRate * Math.pow(1 + monthlyRate, loanTermMonths));
        return maxLoan;
    }
    
    // Ввод семейного положения
    private String getMaritalStatus() {
        System.out.println("\n💑 СЕМЕЙНОЕ ПОЛОЖЕНИЕ:");
        System.out.println("1. Холост/Не замужем");
        System.out.println("2. Женат/Замужем");
        System.out.println("3. Разведен/Разведена");
        System.out.println("4. Вдовец/Вдова");
        System.out.println("5. Гражданский брак");
        
        while (true) {
            try {
                System.out.print("Выберите (1-5): ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1: return "Холост/Не замужем";
                    case 2: return "Женат/Замужем";
                    case 3: return "Разведен/Разведена";
                    case 4: return "Вдовец/Вдова";
                    case 5: return "Гражданский брак";
                    default: System.out.println("❌ Выберите вариант от 1 до 5!");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Введите корректное число!");
            }
        }
    }
    
    // Ввод данных заемщика
    private Borrower inputBorrowerData() {
        System.out.println("\n📝 ВВОД ДАННЫХ ЗАЕМЩИКА");
        System.out.println("-".repeat(35));
        
        // Имя
        String firstName;
        while (true) {
            System.out.print("Введите имя: ");
            firstName = scanner.nextLine().trim();
            if (!firstName.isEmpty()) break;
            System.out.println("❌ Имя не может быть пустым!");
        }
        
        // Фамилия
        String lastName;
        while (true) {
            System.out.print("Введите фамилию: ");
            lastName = scanner.nextLine().trim();
            if (!lastName.isEmpty()) break;
            System.out.println("❌ Фамилия не может быть пустой!");
        }
        
        // Семейное положение
        String maritalStatus = getMaritalStatus();
        
        // Зарплата
        double salaryUsd;
        while (true) {
            try {
                System.out.print("\nВведите зарплату (в долларах США): $");
                salaryUsd = Double.parseDouble(scanner.nextLine().trim());
                
                if (salaryUsd < 0) {
                    System.out.println("❌ Зарплата не может быть отрицательной!");
                    continue;
                }
                
                if (salaryUsd < MIN_SALARY_USD) {
                    System.out.println("\n❌ НЕДОСТАТОЧНЫЙ ДОХОД!");
                    System.out.println("   Минимальная зарплата: $" + MIN_SALARY_USD);
                    System.out.println("   Ваша зарплата: $" + salaryUsd);
                    System.out.println("   Недостает: $" + (MIN_SALARY_USD - salaryUsd));
                    System.out.println("\nВарианты:");
                    System.out.println("1. Ввести зарплату заново");
                    System.out.println("2. Добавить еще одного заемщика позже");
                    System.out.println("3. Выйти из программы");
                    
                    System.out.print("\nВыберите действие (1/2/3): ");
                    String choice = scanner.nextLine().trim();
                    
                    switch (choice) {
                        case "1":
                            continue; // Повторный ввод зарплаты
                        case "2":
                            System.out.println("⚠️ Запомните: общий доход всех заемщиков должен быть достаточным!");
                            break; // Принимаем этого заемщика
                        case "3":
                            System.out.println("\n👋 Программа завершена. До свидания!");
                            return null;
                        default:
                            System.out.println("❌ Неверный выбор, попробуйте еще раз");
                            continue;
                    }
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("❌ Введите корректную сумму (только цифры)!");
            }
        }
        
        // Количество членов семьи
        int familyMembers;
        while (true) {
            try {
                System.out.print("Количество членов семьи: ");
                familyMembers = Integer.parseInt(scanner.nextLine().trim());
                if (familyMembers < 1) {
                    System.out.println("❌ Количество членов семьи должно быть больше 0!");
                    continue;
                }
                if (familyMembers > 20) {
                    System.out.println("❌ Слишком большая семья! Максимум 20 человек.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("❌ Введите корректное число!");
            }
        }
        
        Borrower borrower = new Borrower(firstName, lastName, maritalStatus, salaryUsd, familyMembers);
        borrowers.add(borrower);
        
        System.out.println("\n✅ Заемщик добавлен:");
        System.out.println("   👤 " + borrower.fullName);
        System.out.println("   💑 Семейное положение: " + maritalStatus);
        System.out.println("   💵 Зарплата: $" + salaryUsd + " (≈" + currencyFormat.format(borrower.salaryAzn) + " AZN)");
        System.out.println("   👨‍👩‍👧‍👦 Семья: " + familyMembers + " человек");
        
        return borrower;
    }
    
    // Проверка общего дохода
    private boolean checkTotalIncomeRequirement() {
        double totalUsd = borrowers.stream().mapToDouble(b -> b.salaryUsd).sum();
        double totalAzn = borrowers.stream().mapToDouble(b -> b.salaryAzn).sum();
        
        if (totalUsd < MIN_SALARY_USD) {
            System.out.println("\n❌ ОТКАЗ В КРЕДИТЕ!");
            System.out.println("   Общий доход всех заемщиков: $" + currencyFormat.format(totalUsd) + 
                             " (≈" + currencyFormat.format(totalAzn) + " AZN)");
            System.out.println("   Минимальный требуемый доход: $" + MIN_SALARY_USD);
            System.out.println("   Недостает: $" + currencyFormat.format(MIN_SALARY_USD - totalUsd));
            System.out.println("\n💡 Рекомендации:");
            System.out.println("   - Добавьте еще одного заемщика");
            System.out.println("   - Увеличьте доходы существующих заемщиков");
            System.out.println("   - Обратитесь в банк для индивидуального рассмотрения");
            
            System.out.println("\nНажмите Enter для завершения программы...");
            scanner.nextLine();
            return false;
        }
        
        return true;
    }
    
    // Показать рекомендации по максимальной сумме кредита
    private void showMaxLoanRecommendations() {
        double totalIncome = calculateTotalIncome();
        double maxMonthlyPayment = totalIncome * (MAX_PAYMENT_RATIO / 100);
        
        System.out.println("\n💡 РЕКОМЕНДАЦИИ ПО МАКСИМАЛЬНОЙ СУММЕ КРЕДИТА:");
        System.out.println("   Ваш доход: " + currencyFormat.format(totalIncome) + " AZN/месяц");
        System.out.println("   Максимальный платеж (10%): " + currencyFormat.format(maxMonthlyPayment) + " AZN/месяц");
        System.out.println("-".repeat(60));
        
        for (int months : STANDARD_TERMS) {
            double rate = calculateInterestRate(months);
            double maxLoan = calculateMaxLoanAmount(totalIncome, rate, months);
            String periodText = months < 12 ? months + " мес." : months + " мес. (" + (months/12) + "г.)";
            System.out.printf("   %12s (%4.1f%%): до %8s AZN%n", 
                            periodText, rate, currencyFormat.format(maxLoan));
        }
    }
    
    // Показать таблицу процентных ставок
    private void printRateTable() {
        System.out.println("\n📊 ТАБЛИЦА ПРОЦЕНТНЫХ СТАВОК:");
        System.out.println("-".repeat(45));
        System.out.printf("%-10s %12s %s%n", "Срок", "Ставка", "Описание");
        System.out.println("-".repeat(45));
        
        for (int months : STANDARD_TERMS) {
            double rate = calculateInterestRate(months);
            String periodText = months < 12 ? months + " мес." : 
                              (months % 12 == 0 ? (months/12) + " года" : months + " мес.");
            String description = months <= 6 ? "Короткий срок" : 
                               months <= 18 ? "Средний срок" : "Длинный срок";
            
            System.out.printf("%-10s %10.1f%% %s%n", periodText, rate, description);
        }
        
        System.out.printf("%-10s %12s %s%n", "Свой срок", "11-14%", "Зависит от срока");
        System.out.println("-".repeat(45));
        System.out.println("💡 Чем больше срок, тем меньше процентная ставка");
    }
    
    // Выбор срока кредита
    private int[] getLoanTerm() {
        System.out.println("\n📅 ВЫБОР СРОКА КРЕДИТА:");
        
        // Показать таблицу ставок и рекомендации
        printRateTable();
        showMaxLoanRecommendations();
        
        System.out.println("\nВарианты:");
        for (int i = 0; i < STANDARD_TERMS.length; i++) {
            int term = STANDARD_TERMS[i];
            double rate = calculateInterestRate(term);
            String periodText = term < 12 ? term + " месяцев" : 
                              term + " месяцев (" + (term/12.0) + " года)";
            System.out.println((i+1) + ". " + periodText + " - " + percentFormat.format(rate) + "% годовых");
        }
        
        System.out.println((STANDARD_TERMS.length + 1) + ". Указать свой срок (1-60 месяцев)");
        
        while (true) {
            try {
                System.out.print("\nВыберите вариант (1-" + (STANDARD_TERMS.length + 1) + "): ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                if (choice >= 1 && choice <= STANDARD_TERMS.length) {
                    int selectedTerm = STANDARD_TERMS[choice - 1];
                    double selectedRate = calculateInterestRate(selectedTerm);
                    System.out.println("✅ Выбран срок: " + selectedTerm + " месяцев");
                    System.out.println("✅ Процентная ставка: " + percentFormat.format(selectedRate) + "% годовых");
                    return new int[]{selectedTerm, (int)(selectedRate * 10)}; // Возвращаем ставку * 10 для точности
                } else if (choice == STANDARD_TERMS.length + 1) {
                    // Пользовательский срок
                    while (true) {
                        try {
                            System.out.print("Введите срок в месяцах (1-60): ");
                            int customMonths = Integer.parseInt(scanner.nextLine().trim());
                            if (customMonths < 1 || customMonths > 60) {
                                System.out.println("❌ Срок должен быть от 1 до 60 месяцев!");
                                continue;
                            }
                            
                            double customRate = calculateInterestRate(customMonths);
                            System.out.println("✅ Выбран срок: " + customMonths + " месяцев");
                            System.out.println("✅ Процентная ставка: " + percentFormat.format(customRate) + "% годовых");
                            return new int[]{customMonths, (int)(customRate * 10)};
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Введите корректное число месяцев!");
                        }
                    }
                } else {
                    System.out.println("❌ Выберите вариант от 1 до " + (STANDARD_TERMS.length + 1) + "!");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Введите корректное число!");
            }
        }
    }
    
    // Ввод параметров кредита
    private LoanParams inputLoanParameters() {
        System.out.println("\n💰 ПАРАМЕТРЫ КРЕДИТА");
        System.out.println("-".repeat(30));
        
        // Выбор срока кредита сначала
        int[] loanTermData = getLoanTerm();
        int loanTermMonths = loanTermData[0];
        double annualRate = loanTermData[1] / 10.0;
        
        // Расчет максимально допустимой суммы
        double totalIncome = calculateTotalIncome();
        double maxLoanAmount = calculateMaxLoanAmount(totalIncome, annualRate, loanTermMonths);
        
        System.out.println("\n💡 При выбранном сроке (" + loanTermMonths + " мес., " + 
                          percentFormat.format(annualRate) + "%%):");
        System.out.println("   Максимальная сумма кредита: " + currencyFormat.format(maxLoanAmount) + " AZN");
        System.out.println("   Максимальный платеж: " + currencyFormat.format(totalIncome * 0.1) + 
                          " AZN/месяц (10% от дохода)");
        
        // Сумма кредита
        while (true) {
            try {
                System.out.print("\nВведите желаемую сумму кредита (не более " + 
                               currencyFormat.format(maxLoanAmount) + " AZN): ");
                double loanAmount = Double.parseDouble(scanner.nextLine().trim());
                
                if (loanAmount <= 0) {
                    System.out.println("❌ Сумма должна быть больше 0!");
                    continue;
                }
                
                if (loanAmount > maxLoanAmount) {
                    System.out.println("\n❌ ОТКАЗ В КРЕДИТЕ!");
                    System.out.println("   Запрашиваемая сумма: " + currencyFormat.format(loanAmount) + " AZN");
                    System.out.println("   Максимально допустимая: " + currencyFormat.format(maxLoanAmount) + " AZN");
                    System.out.println("   Превышение: " + currencyFormat.format(loanAmount - maxLoanAmount) + " AZN");
                    System.out.println("\n💡 При данной сумме кредитная нагрузка превысит 10%!");
                    
                    System.out.println("\nВарианты:");
                    System.out.println("1. Ввести сумму заново");
                    System.out.println("2. Выбрать другой срок кредита");
                    System.out.println("3. Завершить программу");
                    
                    System.out.print("\nВыберите действие (1/2/3): ");
                    String choice = scanner.nextLine().trim();
                    switch (choice) {
                        case "1":
                            continue;
                        case "2":
                            // Возвращаемся к выбору срока
                            loanTermData = getLoanTerm();
                            loanTermMonths = loanTermData[0];
                            annualRate = loanTermData[1] / 10.0;
                            maxLoanAmount = calculateMaxLoanAmount(totalIncome, annualRate, loanTermMonths);
                            System.out.println("\n💡 При новом сроке (" + loanTermMonths + " мес., " + 
                                             percentFormat.format(annualRate) + "%%):");
                            System.out.println("   Максимальная сумма кредита: " + 
                                             currencyFormat.format(maxLoanAmount) + " AZN");
                            continue;
                        case "3":
                            System.out.println("\n👋 Программа завершена. До свидания!");
                            return null;
                        default:
                            System.out.println("❌ Неверный выбор!");
                            continue;
                    }
                }
                
                return new LoanParams(loanAmount, annualRate, loanTermMonths, maxLoanAmount);
            } catch (NumberFormatException e) {
                System.out.println("❌ Введите корректную сумму!");
            }
        }
    }
    
    // Расчет общего дохода
    private double calculateTotalIncome() {
        return borrowers.stream().mapToDouble(b -> b.salaryAzn).sum();
    }
    
    // Расчет ежемесячного платежа
    private double calculateMonthlyPayment(double loanAmount, double annualRate, int loanTermMonths) {
        if (annualRate == 0) {
            return loanAmount / loanTermMonths;
        }
        
        double monthlyRate = annualRate / 100 / 12;
        double payment = loanAmount * (monthlyRate * Math.pow(1 + monthlyRate, loanTermMonths)) /
                        (Math.pow(1 + monthlyRate, loanTermMonths) - 1);
        return payment;
    }
    
    // Создание графика платежей
    private List<Payment> generatePaymentSchedule(double loanAmount, double annualRate, int loanTermMonths) {
        double monthlyPayment = calculateMonthlyPayment(loanAmount, annualRate, loanTermMonths);
        double monthlyRate = annualRate / 100 / 12;
        
        List<Payment> schedule = new ArrayList<>();
        double remainingBalance = loanAmount;
        LocalDate startDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int month = 1; month <= loanTermMonths; month++) {
            LocalDate paymentDate = startDate.plusMonths(month - 1);
            
            double interestPayment = remainingBalance * monthlyRate;
            double principalPayment = monthlyPayment - interestPayment;
            remainingBalance -= principalPayment;
            
            // Корректировка последнего платежа
            if (month == loanTermMonths && remainingBalance != 0) {
                principalPayment += remainingBalance;
                monthlyPayment = interestPayment + principalPayment;
                remainingBalance = 0;
            }
            
            schedule.add(new Payment(
                month,
                paymentDate.format(formatter),
                Math.round(monthlyPayment * 100.0) / 100.0,
                Math.round(principalPayment * 100.0) / 100.0,
                Math.round(interestPayment * 100.0) / 100.0,
                Math.round(Math.max(0, remainingBalance) * 100.0) / 100.0
            ));
        }
        
        return schedule;
    }
    
    // Анализ доступности кредита
    private AffordabilityAnalysis analyzeAffordability(double monthlyPayment) {
        double totalIncome = calculateTotalIncome();
        if (totalIncome == 0) {
            return null;
        }
        
        double paymentRatio = (monthlyPayment / totalIncome) * 100;
        
        // Учет семейного положения
        long marriedCount = borrowers.stream()
            .filter(b -> b.maritalStatus.contains("Женат") || b.maritalStatus.contains("Замужем"))
            .count();
        String familyBonus = marriedCount > 0 ? 
            " Семейный статус обеспечивает дополнительную стабильность." : "";
        
        String status = "✅ КРЕДИТ ОДОБРЕН";
        String recommendation = "Кредитная нагрузка " + percentFormat.format(paymentRatio) + 
                              "% - в пределах допустимого лимита 10%." + familyBonus;
        
        return new AffordabilityAnalysis(
            totalIncome,
            monthlyPayment,
            paymentRatio,
            totalIncome - monthlyPayment,
            status,
            recommendation
        );
    }
    
    // Вывод анализа по заемщикам
    private void printBorrowersAnalysis(double monthlyPayment) {
        double totalIncome = calculateTotalIncome();
        
        System.out.println("\n👥 АНАЛИЗ НАГРУЗКИ ПО ЗАЕМЩИКАМ:");
        System.out.println("-".repeat(65));
        
        for (int i = 0; i < borrowers.size(); i++) {
            Borrower borrower = borrowers.get(i);
            double share = borrower.salaryAzn / totalIncome;
            double borrowerPayment = monthlyPayment * share;
            double borrowerRatio = (borrowerPayment / borrower.salaryAzn) * 100;
            double remaining = borrower.salaryAzn - borrowerPayment;
            
            System.out.println("\n" + (i+1) + ". " + borrower.fullName);
            System.out.println("   💑 Семейное положение: " + borrower.maritalStatus);
            System.out.println("   💵 Зарплата: $" + borrower.salaryUsd + 
                             " (" + currencyFormat.format(borrower.salaryAzn) + " AZN)");
            System.out.println("   📊 Доля в семейном доходе: " + percentFormat.format(share * 100) + "%");
            System.out.println("   💳 Доля кредитного платежа: " + currencyFormat.format(borrowerPayment) + " AZN");
            System.out.println("   📈 Нагрузка на доход: " + percentFormat.format(borrowerRatio) + "%");
            System.out.println("   💰 Остаток после платежа: " + currencyFormat.format(remaining) + 
                             " AZN (90%+ дохода)");
        }
    }
    
    // Вывод таблицы платежей
    private void printPaymentTable(List<Payment> schedule, int startMonth, int count) {
        int endMonth = Math.min(startMonth + count - 1, schedule.size());
        
        System.out.println("\n📅 ГРАФИК ПЛАТЕЖЕЙ (месяцы " + startMonth + "-" + endMonth + "):");
        System.out.println("-".repeat(85));
        System.out.printf("%3s %12s %15s %15s %12s %15s%n", 
                         "№", "Дата", "Платеж (AZN)", "Основной долг", "Проценты", "Остаток");
        System.out.println("-".repeat(85));
        
        for (int i = startMonth - 1; i < endMonth; i++) {
            Payment payment = schedule.get(i);
            System.out.printf("%3d %12s %15s %15s %12s %15s%n",
                             payment.month,
                             payment.date,
                             currencyFormat.format(payment.monthlyPayment),
                             currencyFormat.format(payment.principal),
                             currencyFormat.format(payment.interest),
                             currencyFormat.format(payment.balance));
        }
    }
    
    // Вывод семейной информации
    private void printFamilySummary() {
        System.out.println("\n👨‍👩‍👧‍👦 СЕМЕЙНАЯ ИНФОРМАЦИЯ:");
        System.out.println("-".repeat(40));
        
        int totalFamilyMembers = borrowers.stream().mapToInt(b -> b.familyMembers).sum();
        long marriedCount = borrowers.stream()
            .filter(b -> b.maritalStatus.contains("Женат") || b.maritalStatus.contains("Замужем"))
            .count();
        
        System.out.println("Общее количество членов семьи: " + totalFamilyMembers);
        System.out.println("Заемщиков в браке: " + marriedCount + " из " + borrowers.size());
        
        if (marriedCount > 0) {
            System.out.println("✅ Семейный статус повышает кредитную надежность");
        }
        
        // Статистика по семейному положению
        Map<String, Integer> statusCount = new HashMap<>();
        for (Borrower borrower : borrowers) {
            statusCount.put(borrower.maritalStatus, 
                           statusCount.getOrDefault(borrower.maritalStatus, 0) + 1);
        }
        
        System.out.println("\nРаспределение по семейному положению:");
        for (Map.Entry<String, Integer> entry : statusCount.entrySet()) {
            System.out.println("- " + entry.getKey() + ": " + entry.getValue() + " чел.");
        }
    }
    
    // Вывод информации о финансовой безопасности
    private void printFinancialSafety(AffordabilityAnalysis analysis) {
        System.out.println("\n💰 ФИНАНСОВАЯ БЕЗОПАСНОСТЬ:");
        System.out.println("-".repeat(50));
        double remainingPercentage = 100 - analysis.paymentRatio;
        System.out.println("Остается от дохода: " + percentFormat.format(remainingPercentage) + 
                          "% (" + currencyFormat.format(analysis.remainingIncome) + " AZN)");
        System.out.println("Кредитная нагрузка: " + percentFormat.format(analysis.paymentRatio) + 
                          "% (лимит: " + percentFormat.format(MAX_PAYMENT_RATIO) + "%)");
        System.out.println("Запас до лимита: " + 
                          percentFormat.format(MAX_PAYMENT_RATIO - analysis.paymentRatio) + "%");
        
        if (remainingPercentage >= 90) {
            System.out.println("✅ Отличная финансовая устойчивость");
        } else if (remainingPercentage >= 85) {
            System.out.println("✅ Хорошая финансовая устойчивость");
        } else {
            System.out.println("⚠️ Умеренная финансовая устойчивость");
        }
    }
    
    // Сохранение в файл
    private void saveToFile(LoanParams loanParams, List<Payment> schedule, 
                           AffordabilityAnalysis analysis, double monthlyPayment) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("loan_approved_azn_v3_java.txt", 
                                                  StandardCharsets.UTF_8))) {
            writer.println("ОДОБРЕННЫЙ КРЕДИТ - ГРАФИК ПЛАТЕЖЕЙ (АЗЕРБАЙДЖАН) v3.0 Java Edition");
            writer.println("=".repeat(75));
            writer.println();
            writer.println("СТАТУС: КРЕДИТ ОДОБРЕН!");
            writer.println("Максимальная кредитная нагрузка: " + percentFormat.format(MAX_PAYMENT_RATIO) + "%");
            writer.println();
            
            writer.println("Заемщики:");
            for (Borrower borrower : borrowers) {
                writer.println("- " + borrower.fullName + ": $" + borrower.salaryUsd + 
                             " (" + currencyFormat.format(borrower.salaryAzn) + " AZN)");
                writer.println("  Семейное положение: " + borrower.maritalStatus);
                writer.println("  Семья: " + borrower.familyMembers + " человек");
                writer.println();
            }
            
            writer.println("Параметры кредита:");
            writer.println("- Сумма: " + currencyFormat.format(loanParams.amount) + " AZN");
            writer.println("- Ставка: " + percentFormat.format(loanParams.rate) + "% (зависит от срока)");
            writer.println("- Срок: " + loanParams.months + " месяцев");
            writer.println("- Ежемесячный платеж: " + currencyFormat.format(monthlyPayment) + " AZN");
            writer.println("- Кредитная нагрузка: " + percentFormat.format(analysis.paymentRatio) + "%");
            writer.println();
            
            writer.println("МЕСЯЦ\tДАТА\t\tПЛАТЕЖ (AZN)\tОСНОВНОЙ ДОЛГ\tПРОЦЕНТЫ\tОСТАТОК");
            writer.println("-".repeat(80));
            for (Payment payment : schedule) {
                writer.printf("%d\t%s\t%.2f\t\t%.2f\t%.2f\t%.2f%n",
                             payment.month, payment.date, payment.monthlyPayment,
                             payment.principal, payment.interest, payment.balance);
            }
            
            System.out.println("\n✅ График сохранен в файл: loan_approved_azn_v3_java.txt");
        } catch (IOException e) {
            System.out.println("\n⚠️ Ошибка сохранения файла: " + e.getMessage());
        }
    }
    
    // Основная функция программы
    public void run() {
        try {
            clearScreen();
            printHeader("КРЕДИТНЫЙ КАЛЬКУЛЯТОР (АЗЕРБАЙДЖАН) v3.0 Java Edition");
            
            System.out.println("\n💰 Минимальная зарплата: $" + MIN_SALARY_USD);
            System.out.println("💱 Курс доллара: " + USD_TO_AZN + " AZN");
            System.out.println("📈 Процентные ставки: " + percentFormat.format(MIN_RATE) + "% - " + 
                             percentFormat.format(MAX_RATE) + "%");
            System.out.println("🚨 Максимальная кредитная нагрузка: " + percentFormat.format(MAX_PAYMENT_RATIO) + "%");
            System.out.println("🏦 Расчеты производятся в манатах (AZN)");
            System.out.println("\n💡 Принцип работы: Только 10% дохода на кредит, 90% остается вам!");
            
            // Ввод количества заемщиков
            int numBorrowers;
            while (true) {
                try {
                    System.out.print("\nСколько заемщиков будет? (1-5): ");
                    numBorrowers = Integer.parseInt(scanner.nextLine().trim());
                    if (numBorrowers >= 1 && numBorrowers <= 5) {
                        break;
                    }
                    System.out.println("❌ Количество заемщиков должно быть от 1 до 5!");
                } catch (NumberFormatException e) {
                    System.out.println("❌ Введите корректное число!");
                }
            }
            
            // Ввод данных заемщиков
            for (int i = 0; i < numBorrowers; i++) {
                System.out.println("\n" + "=".repeat(20) + " ЗАЕМЩИК " + (i+1) + " " + "=".repeat(20));
                Borrower borrower = inputBorrowerData();
                if (borrower == null) {
                    return; // Пользователь выбрал выход
                }
            }
            
            // Проверка общего дохода
            if (!checkTotalIncomeRequirement()) {
                return;
            }
            
            // Ввод параметров кредита
            LoanParams loanParams = inputLoanParameters();
            if (loanParams == null) {
                return; // Пользователь завершил программу
            }
            
            // Расчеты
            double monthlyPayment = calculateMonthlyPayment(
                loanParams.amount, loanParams.rate, loanParams.months);
            
            List<Payment> schedule = generatePaymentSchedule(
                loanParams.amount, loanParams.rate, loanParams.months);
            
            AffordabilityAnalysis analysis = analyzeAffordability(monthlyPayment);
            
            // Результаты
            clearScreen();
            printHeader("КРЕДИТ ОДОБРЕН! РЕЗУЛЬТАТЫ РАСЧЕТА");
            
            System.out.println("\n🎉 ПОЗДРАВЛЯЕМ! ВАШ КРЕДИТ ОДОБРЕН!");
            System.out.println("💰 ПАРАМЕТРЫ КРЕДИТА:");
            System.out.println("   Сумма: " + currencyFormat.format(loanParams.amount) + " AZN");
            System.out.println("   Ставка: " + percentFormat.format(loanParams.rate) + "% годовых");
            System.out.println("   Срок: " + loanParams.months + " месяцев (" + 
                             percentFormat.format(loanParams.years) + " года)");
            System.out.println("   Максимально было доступно: " + 
                             currencyFormat.format(loanParams.maxAllowed) + " AZN");
            
            System.out.println("\n📊 ФИНАНСОВЫЙ АНАЛИЗ:");
            System.out.println("   Общий доход семьи: " + currencyFormat.format(analysis.totalIncome) + " AZN/месяц");
            System.out.println("   Ежемесячный платеж: " + currencyFormat.format(analysis.monthlyPayment) + " AZN");
            System.out.println("   Кредитная нагрузка: " + percentFormat.format(analysis.paymentRatio) + 
                             "% (лимит: " + percentFormat.format(MAX_PAYMENT_RATIO) + "%)");
            System.out.println("   Остается свободных средств: " + currencyFormat.format(analysis.remainingIncome) + 
                             " AZN (" + percentFormat.format(100 - analysis.paymentRatio) + "%)");
            System.out.println("   Статус: " + analysis.status);
            System.out.println("   💡 " + analysis.recommendation);
            
            // Финансовая безопасность
            printFinancialSafety(analysis);
            
            // Семейная информация
            printFamilySummary();
            
            // Анализ по заемщикам
            printBorrowersAnalysis(monthlyPayment);
            
            // Таблицы платежей
            if (loanParams.months <= 12) {
                printPaymentTable(schedule, 1, loanParams.months); // Все месяцы
            } else {
                printPaymentTable(schedule, 1, 12); // Первые 12 месяцев
                if (loanParams.months > 24) {
                    printPaymentTable(schedule, loanParams.months - 11, 12); // Последние 12 месяцев
                }
            }
            
            // Итоговая статистика
            double totalPayments = schedule.stream().mapToDouble(p -> p.monthlyPayment).sum();
            double totalInterest = schedule.stream().mapToDouble(p -> p.interest).sum();
            
            System.out.println("\n📈 ИТОГОВАЯ СТАТИСТИКА:");
            System.out.println("-".repeat(55));
            System.out.println("Общая сумма выплат: " + currencyFormat.format(totalPayments) + " AZN");
            System.out.println("Переплата по процентам: " + currencyFormat.format(totalInterest) + " AZN");
            System.out.println("Коэффициент переплаты: " + 
                             percentFormat.format((totalPayments / loanParams.amount - 1) * 100) + "%");
            System.out.println("Общая экономия семьи за срок кредита: " + 
                             currencyFormat.format(analysis.remainingIncome * loanParams.months) + " AZN");
            
            // Сохранение в файл
            saveToFile(loanParams, schedule, analysis, monthlyPayment);
            
            System.out.println("\n🎉 КРЕДИТ УСПЕШНО ОДОБРЕН И РАССЧИТАН!");
            System.out.println("💡 Помните: у вас остается " + 
                             percentFormat.format(100 - analysis.paymentRatio) + "% дохода для других расходов!");
            System.out.println("\nНажмите Enter для выхода...");
            scanner.nextLine();
            
        } catch (Exception e) {
            System.out.println("\n❌ Произошла ошибка: " + e.getMessage());
            System.out.println("Попробуйте запустить программу заново.");
            System.out.println("\nНажмите Enter для выхода...");
            scanner.nextLine();
        } finally {
            scanner.close();
        }
    }
    
    // Точка входа в программу
    public static void main(String[] args) {
        LoanCalculator calculator = new LoanCalculator();
        calculator.run();
    }
}