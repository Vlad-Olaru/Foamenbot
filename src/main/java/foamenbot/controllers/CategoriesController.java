package foamenbot.controllers;

import foamenbot.model.*;
import foamenbot.repositories.ProductRepository;
import foamenbot.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Set;

@Controller
public class CategoriesController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @ModelAttribute("currentUser")
    private User getCurrentUser() {
        return userService.findByUserName(SecurityContextHolder.getContext().getAuthentication().getName()).get(0);
    }

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderProductService orderProductService;

    @ModelAttribute("activeOrders")
    private Set<Order> getActiveOrders() {
        return orderService.findByStatus("active");
    }

    @ModelAttribute("categories")
    private List<Category> getContacts() {
        return categoryService.findAll();
    }

    @RequestMapping(value = {"/categories"}, method = RequestMethod.GET)
    public String getCategoriesPage() {
        return "categories";
    }

    @RequestMapping(value = {"/categories/{id}"}, method = RequestMethod.GET)
    public String getProductsForEachCategory(@PathVariable long id, Model model) {
        Category currentCategory = categoryService.findById(id);
        List<Product> products = productService.findByCategory(currentCategory);
        model.addAttribute("category", currentCategory);
        model.addAttribute("products", products);
        return "productsView";
    }

    @RequestMapping(value = {"/addProduct/{productId}/{orderId}"}, method = RequestMethod.POST, params="action=add")
    public String addProductToOrder(@PathVariable long productId, @PathVariable long orderId, Model model) {
            Product product = productService.findById(productId);
            Order order = orderService.findById(orderId);
            if(orderProductService.findByProductAndOrder(product, order) == null) {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.setOrder(order);
                orderProduct.setProduct(product);
                orderProduct.setQuantity(1);
                order.setTotalPrice(order.getTotalPrice()+product.getPrice());
                orderProductService.save(orderProduct);
                orderService.save(order);
                return "redirect:/categories";
            }
            else {
                OrderProduct orderProduct = orderProductService.findByProductAndOrder(product, order);
                orderProduct.setQuantity(orderProduct.getQuantity() + 1);
                order.setTotalPrice(order.getTotalPrice()+product.getPrice());
                orderProductService.save(orderProduct);
                orderProductService.save(orderProduct);
                return "redirect:/categories";
            }
    }

    @RequestMapping(value = {"addProduct/{productId}/{orderId}"}, method = RequestMethod.POST, params="action=edit")
    public String getEditProductPage(@PathVariable long productId, Model model) {
        return "redirect:/edit/" + productId;
    }

}
