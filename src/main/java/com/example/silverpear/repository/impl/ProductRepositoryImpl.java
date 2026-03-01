/*import com.example.silverpear.product.entity.Product;
import com.example.silverpear.repository.ProductRepository;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
//import java.util.Optional;

@Repository
@Transactional
public class ProductRepositoryImpl implements ProductRepository {

    @PersistenceContext
    private EntityManager em;

    //@Override
    public List<Product> findByBrand(String brand) {  // <- добавили public!
        return em.createQuery("SELECT p FROM Product p WHERE p.brand = :brand", Product.class)
                .setParameter("brand", brand)
                .getResultList();
    }
}
*/