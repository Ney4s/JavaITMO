package neyas.itmo.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import neyas.itmo.entity.BaseEntity;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.framework.Positive;
import neyas.itmo.framework.PositiveOrZero;

import java.util.LinkedHashSet;
import java.util.Set;


@Entity
@Table(name = "spare_parts")
@Getter
@NoArgsConstructor
public class SparePart extends BaseEntity {

    @Column(nullable = false, unique = true)
    private SparePartId code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column(name = "quantity_in_stock", nullable = false)
    private int quantityInStock;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "spare_part_models",
            joinColumns = @JoinColumn(name = "spare_part_id"),
            inverseJoinColumns = @JoinColumn(name = "model_id"))
    private Set<Model> compatibleModels = new LinkedHashSet<>();

    public SparePart(SparePartId code,
                     String name,
                     long price,
                     int quantityInStock,
                     Set<Model> compatibleModels) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantityInStock = quantityInStock;
        this.compatibleModels = new LinkedHashSet<>(compatibleModels);
    }

    public long price(){
        return price;
    }

    public int quantityInStock(){
        return quantityInStock;
    }

    public boolean fitsTo(ModelId modelId) {
        return compatibleModels.stream()
                .anyMatch(model -> model.code().equals(modelId));
    }
    public boolean isInStock(int requestedQuantity) {
        return quantityInStock >= requestedQuantity;
    }

    public void changeQuantity(@PositiveOrZero int newQuantity) {
        this.quantityInStock = newQuantity;
    }
    public void takeFromStock(@Positive int quantity) {
        this.quantityInStock -= quantity;
    }
}
