public class Food {
    private final int id;
    private String name, category;
    private double price;

    public Food(int id, String name, String category, double price) {
        this.id = id; this.name = name; this.category = category; this.price = price;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public String toString() { return name + " - Rs. " + String.format("%.2f", price); }
}
