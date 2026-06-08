import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StockTrading{
    private static final Map<String, Stock> stocks = new HashMap<>();
    private static final Map<String, User> users = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    public static void main(String[] args) {
        initializeStocks();
        startMarketSimulation();

        while (true) {
            System.out.println("\n1. Register User");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    System.out.println("Exiting program. Goodbye!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void initializeStocks() {
        stocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 150.0));
        stocks.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 2800.0));
        stocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 3300.0));
        stocks.put("MSFT", new Stock("MSFT", "Microsoft Corporation", 300.0));
        stocks.put("TSLA", new Stock("TSLA", "Tesla, Inc.", 700.0));
    }

    private static void startMarketSimulation() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStockPrices();
            }
        }, 0, 5000); // Update every 5 seconds
    }

    private static void updateStockPrices() {
        for (Stock stock : stocks.values()) {
            double change = (random.nextDouble() - 0.5) * 5; // Random change between -2.5% and 2.5%
            stock.updatePrice(change);
        }
    }

    private static void registerUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter initial balance: ");
        double balance = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        users.put(username, new User(username, balance));
        System.out.println("User registered successfully.");
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        User user = users.get(username);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        userMenu(user);
    }

    private static void userMenu(User user) {
        while (true) {
            System.out.println("\n1. View Market Data");
            System.out.println("2. View Portfolio");
            System.out.println("3. Buy Stock");
            System.out.println("4. Sell Stock");
            System.out.println("5. View Performance");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> viewMarketData();
                case 2 -> user.viewPortfolio();
                case 3 -> buyStock(user);
                case 4 -> sellStock(user);
                case 5 -> user.viewPerformance();
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void viewMarketData() {
        System.out.println("Current Market Data:");
        for (Stock stock : stocks.values()) {
            System.out.println(stock);
        }
    }

    private static void buyStock(User user) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine().toUpperCase();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Stock stock = stocks.get(symbol);
        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }

        double totalCost = stock.getPrice() * quantity;
        if (user.getBalance() < totalCost) {
            System.out.println("Insufficient funds.");
            return;
        }

        user.buyStock(stock, quantity);
        System.out.println("Stock purchased successfully.");
    }

    private static void sellStock(User user) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine().toUpperCase();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Stock stock = stocks.get(symbol);
        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }

        if (user.sellStock(stock, quantity)) {
            System.out.println("Stock sold successfully.");
        } else {
            System.out.println("Insufficient stocks to sell.");
        }
    }
}

class Stock {
    private final String symbol;
    private final String name;
    private double price;
    private final double openPrice;
    private final List<Double> priceHistory = new ArrayList<>();

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.openPrice = price;
        this.priceHistory.add(price);
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public void updatePrice(double change) {
        price += change;
        if (price < 0) price = 0.01; // Prevent negative prices
        priceHistory.add(price);
    }

    public double getPercentChange() {
        return ((price - openPrice) / openPrice) * 100;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): $%.2f (%.2f%%)", symbol, name, price, getPercentChange());
    }

    public List<Double> getPriceHistory() {
        return priceHistory;
    }
}

class User {
    private final String username;
    private double balance;
    private final Map<Stock, Integer> portfolio = new HashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    public User(String username, double balance) {
        this.username = username;
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public void buyStock(Stock stock, int quantity) {
        double totalCost = stock.getPrice() * quantity;
        balance -= totalCost;
        portfolio.put(stock, portfolio.getOrDefault(stock, 0) + quantity);
        transactions.add(new Transaction(stock, quantity, stock.getPrice(), TransactionType.BUY));
    }

    public boolean sellStock(Stock stock, int quantity) {
        int currentQuantity = portfolio.getOrDefault(stock, 0);
        if (currentQuantity < quantity) {
            return false;
        }
        double totalEarnings = stock.getPrice() * quantity;
        balance += totalEarnings;
        portfolio.put(stock, currentQuantity - quantity);
        transactions.add(new Transaction(stock, quantity, stock.getPrice(), TransactionType.SELL));
        return true;
    }

    public void viewPortfolio() {
        System.out.println("Current Balance: $" + String.format("%.2f", balance));
        System.out.println("Portfolio:");
        for (Map.Entry<Stock, Integer> entry : portfolio.entrySet()) {
            Stock stock = entry.getKey();
            int quantity = entry.getValue();
            double totalValue = stock.getPrice() * quantity;
            System.out.printf("%s: %d shares (Total Value: $%.2f)\n",
                    stock.getSymbol(), quantity, totalValue);
        }
    }

    public void viewPerformance() {
        double totalInvestment = 0;
        double currentValue = 0;
        for (Map.Entry<Stock, Integer> entry : portfolio.entrySet()) {
            Stock stock = entry.getKey();
            int quantity = entry.getValue();
            totalInvestment += getAveragePurchasePrice(stock) * quantity;
            currentValue += stock.getPrice() * quantity;
        }
        double performancePercent = ((currentValue - totalInvestment) / totalInvestment) * 100;
        System.out.printf("Total Investment: $%.2f\n", totalInvestment);
        System.out.printf("Current Value: $%.2f\n", currentValue);
        System.out.printf("Performance: %.2f%%\n", performancePercent);
    }

    private double getAveragePurchasePrice(Stock stock) {
        double totalCost = 0;
        int totalQuantity = 0;
        for (Transaction t : transactions) {
            if (t.getStock().equals(stock) && t.getType() == TransactionType.BUY) {
                totalCost += t.getPrice() * t.getQuantity();
                totalQuantity += t.getQuantity();
            }
        }
        return totalQuantity > 0 ? totalCost / totalQuantity : 0;
    }

    public String getUsername() {
        return username;
    }
}

class Transaction {
    private final Stock stock;
    private final int quantity;
    private final double price;
    private final TransactionType type;
    private final LocalDateTime timestamp;

    public Transaction(Stock stock, int quantity, double price, TransactionType type) {
        this.stock = stock;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public Stock getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public TransactionType getType() {
        return type;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%s - %s %d %s at $%.2f", 
            timestamp.format(formatter), type, quantity, stock.getSymbol(), price);
    }
}

enum TransactionType {
    BUY, SELL
}