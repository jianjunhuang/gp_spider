import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GooglePlaySearchProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(2).setSleepTime(1000);
    private Logger logger = Logger.getLogger(getClass());
    Map<String, Integer> target;
    WebDriver webDriver;
    BufferedWriter writer = null;

    public void process(Page page) {
        if (webDriver == null) {
            webDriver = new ChromeDriver();
        }
        String pageUrl = page.getUrl().toString();
        logger.info(pageUrl);
        webDriver.get(pageUrl);
        webDriver.manage().window().maximize();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 5; i++) {
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            js.executeScript("scrollTo(0,10000)");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        List<WebElement> elements = webDriver.findElements(By.xpath("//a[contains(@class,'poRVub')]"));
        logger.info("Element size = " + elements.size());
//        for (WebElement element : elements) {
//            logger.debug(element.getAttribute("href"));
//        }
//        List<String> urls = page.getHtml().xpath("//a[contains(@class,'poRVub')]/@href").all();
//        logger.info("size = " + urls.size());
        for (int i = 0; i < elements.size(); i++) {
            String url = elements.get(i).getAttribute("href");
            String pkg = url.substring(url.indexOf("=") + 1);
            if (target.containsKey(pkg)) {
                target.put(pkg, i + 1);
            }
            logger.info(pkg);
        }
        logRes(pageUrl.substring(pageUrl.indexOf("=") + 1));
    }

    public Site getSite() {
        return site;
    }

    public static void main(String args[]) {
        System.setProperty("webdriver.chrome.driver", "/home/jianjunhuang/code/java/gp_spider/src/main/resources/chromedriver");
        GooglePlaySearchProcessor processor = new GooglePlaySearchProcessor();
        processor.createTarget();

        Spider.create(processor).addUrl(processor.getKeywords()).thread(1).run();
    }

    public void createTarget() {
        File targetFile = new File("/home/jianjunhuang/code/java/gp_spider/src/main/resources/target.properties");
        File dir = new File("");
        logger.info(dir.getAbsolutePath());
        target = new ConcurrentHashMap<String, Integer>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile)));
            String pkg;
            while ((pkg = reader.readLine()) != null) {
                if (!pkg.startsWith("#")) {
                    target.put(pkg, -1);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String[] getKeywords() {
        File targetFile = new File("/home/jianjunhuang/code/java/gp_spider/src/main/resources/keyword.txt");
        List<String> keywords = new ArrayList<String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile)));
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                keywords.add("https://play.google.com/store/search?q=" + keyword);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return keywords.toArray(new String[0]);
    }

    public void logRes(String key) {
        try {
            if (writer == null) {
                File file = new File("res.csv");
                if (file.exists()) {
                    boolean isDel = file.delete();
                }
                boolean isCreate = file.createNewFile();
                writer = new BufferedWriter(new FileWriter(file));
            }
            for (Map.Entry<String, Integer> entry : target.entrySet()) {
                String ans = "\"" + entry.getKey() + "\"" + ",\"" + key + "\",\"" + entry.getValue() + "\"\n";
                logger.info(ans);
                try {
                    writer.append(ans);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer.flush();
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
        }


    }
}
