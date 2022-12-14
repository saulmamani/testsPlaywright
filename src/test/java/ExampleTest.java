import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Objects;

public class ExampleTest {
    private static Page login(Playwright playwright) {
        Page page = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50)).newPage();
        page.setViewportSize(1520, 720);
        page.navigate("https://www.saucedemo.com/");

        page.type("[placeholder=Username]", "standard_user");
        page.type("[placeholder=Password]", "secret_sauce");
        page.click("input[type='submit']");
        return page;
    }

    //1. Comprobar que el producto Sauce Labs Onesie existe y que tenga un costo de 7.99
    @Test
    public void existProduct(){
        try(Playwright playwright = Playwright.create()) {
            Page page = login(playwright);

            boolean isProduct = Objects.equals(page.innerText("#item_2_title_link div"), "Sauce Labs Onesie");

            page.click("#item_2_img_link img");

            boolean isPrice = page.innerText(".inventory_details_price").contains("7.99");

            Assert.assertTrue(isProduct && isPrice);
        }
    }

    //2. Insertar 5 productos en el carrito, y comprobar que los 5 elementos esten registrados en el carrito
    @Test
    public void insertProductsToCart()
    {
        try(Playwright playwright = Playwright.create()) {
            Page page = login(playwright);

            addProductToCart(page);

            int productsInCart = Integer.parseInt(page.innerText(".shopping_cart_badge"));
            Assert.assertEquals(productsInCart, 5);
        }
    }

    //3. Insertar 5 productos en el carrito, eliminar 2 productos y verificar si se han eliminado
    @Test
    public void deleteProductsToCart()
    {
        try(Playwright playwright = Playwright.create()) {
            Page page = login(playwright);

            addProductToCart(page);

            int productsInCart = Integer.parseInt(page.innerText(".shopping_cart_badge"));

            //eliminado
            page.click("a.shopping_cart_link");
            page.click("#remove-sauce-labs-bike-light");
            page.click("#remove-sauce-labs-backpack");

            int productsAfterDelete = Integer.parseInt(page.innerText(".shopping_cart_badge"));

            boolean result = productsInCart == 5 && productsAfterDelete == 3;

            Assert.assertTrue(result);
        }
    }

    //4. Registrar los datos del cliente y mostrar el resumen de la compra
    @Test
    public void registerClient()
    {
        try(Playwright playwright = Playwright.create()) {
            Page page = login(playwright);

            addProductToCart(page);

            page.click("a.shopping_cart_link");

            page.click("#checkout");

            //registrar datos del cliente
            registerClient(page);

            page.click("#continue");

            boolean isSummaryVisible = page.isVisible("text=FREE PONY EXPRESS DELIVERY!");

            Assert.assertTrue(isSummaryVisible);
        }
    }

    //4. Registrar y finalizar el processo de una compra
    @Test
    public void finishProccess()
    {
        try(Playwright playwright = Playwright.create()) {
            Page page = login(playwright);

            addProductToCart(page);

            page.click("a.shopping_cart_link");

            page.click("#checkout");

            //registrar datos del cliente
            registerClient(page);

            page.click("#continue");
            page.click("#finish");

            boolean isFinish = page.isVisible("text=THANK YOU FOR YOUR ORDER");

            Assert.assertTrue(isFinish);
        }
    }

    //6. Insertar 5 productos en el carrito, y verificar que la suma de los precios sea igual al total mostrado
    @Test
    public void sumProductsToCart()
    {
        try(Playwright playwright = Playwright.create()) {
            Page page = login(playwright);

            addProductToCart(page);

            int productsInCart = Integer.parseInt(page.innerText(".shopping_cart_badge"));

            page.click("a.shopping_cart_link");

            //extraendo precios
            var precios = page.locator(".inventory_item_price").allInnerTexts();

            double total = 0.0;
            for(String p:precios){
                float precioProducto = Float.parseFloat(p.substring(1));
                total += precioProducto;
            }

            System.out.println(((double) Math.round(total * 100) / 100));

            //registrar datos del cliente
            page.click("#checkout");
            registerClient(page);
            page.click("#continue");

            String totalPagina = page.innerText(".summary_subtotal_label");
            double tp = (double)Math.round(Float.parseFloat(totalPagina.substring(13)) * 100) / 100;
            System.out.println(tp);

            boolean result = ((double) Math.round(total * 100) / 100) == tp;

            Assert.assertTrue(result);
        }
    }

    private static void registerClient(Page page) {
        page.type("#first-name", "Saul");
        page.type("#last-name", "Mamani");
        page.type("#postal-code", "591 7612345");
    }

    private static void addProductToCart(Page page) {
        page.click("#add-to-cart-sauce-labs-backpack");
        page.click("#add-to-cart-sauce-labs-bike-light");
        page.click("#add-to-cart-sauce-labs-bolt-t-shirt");
        page.click("#add-to-cart-sauce-labs-onesie");
        page.click("#add-to-cart-sauce-labs-fleece-jacket");
    }
}
