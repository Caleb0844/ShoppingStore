package com.caleb.Creator_store.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.caleb.Creator_store.Entities.Product;
import com.caleb.Creator_store.Repositories.ProductRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ProductService {

    private final ProductRepo productRepo;
    public Product createProduct(Product product){
        return productRepo.save(product);
    }

    public Product updateProduct(Long id,Product product){
        Product existingProduct = productRepo.findById(id)
        .orElseThrow(()-> new RuntimeException("Product with id " +id+ "not found!"));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());

        return productRepo.save(existingProduct);

    }

    public List<Product> getProducts(){
        return productRepo.findAll();
    }

    public Product getProductById(Long id){
        return productRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("Product with id " +id+ "not found!"));


    }
    //public Product deleteProduct(){
       // return null;
   // }

    public void deleteProductById(Long id){
        productRepo.deleteById(id);
    }

}
