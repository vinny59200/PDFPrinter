
import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.utils.ImageUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import lombok.Data;
import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * __________________  ______     _       _
 * | ___ \  _  \  ___| | ___ \   (_)     | |
 * | |_/ / | | | |_    | |_/ / __ _ _ __ | |_ ___ _ __
 * |  __/| | | |  _|   |  __/ '__| | '_ \| __/ _ \ '__|
 * | |   | |/ /| |     | |  | |  | | | | | ||  __/ |
 * \_|   |___/ \_|     \_|  |_|  |_|_| |_|\__\___|_|
 *
 * @author vvauban
 */
//TODO handle the page breaks
@Data
public class PDFPrinter {

    /*__ _      _     _
     / _(_)    | |   | |
    | |_ _  ___| | __| |___
    |  _| |/ _ \ |/ _` / __|
    | | | |  __/ | (_| \__ \
    |_| |_|\___|_|\__,_|___/
     */
    private float pageHeight = 0;
    private float currentPrintingHeight = 0;
    private PDDocument currentDocument;
    private PDPage currentPage;
    private PDPageContentStream currentContentStream;
    private float margin = 50;
    private float yStartNewPage = 0;
    private float tableWidth = 0;
    private boolean drawContent = true;
    private float bottomMargin = 70;


    /*
     _ __ _   _ _ __  _ __   ___ _ __
    | '__| | | | '_ \| '_ \ / _ \ '__|
    | |  | |_| | | | | | | |  __/ |
    |_|   \__,_|_| |_|_| |_|\___|_|
     */
    public static void main(String[] args) {
        try {
            new PDFPrinter().print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*                        _                        _   _               _
     _ __  _ __(_)_   ____ _| |_ ___   _ __ ___   ___| |_| |__   ___   __| |___
    | '_ \| '__| \ \ / / _` | __/ _ \ | '_ ` _ \ / _ \ __| '_ \ / _ \ / _` / __|
    | |_) | |  | |\ V / (_| | ||  __/ | | | | | |  __/ |_| | | | (_) | (_| \__ \
    | .__/|_|  |_| \_/ \__,_|\__\___| |_| |_| |_|\___|\__|_| |_|\___/ \__,_|___/
    |_|
    */
    private void print() throws Exception {
        PDDocument doc = new PDDocument();
        PDPage blankPage = new PDPage();
        //Adding the blank page to the document
        doc.addPage(blankPage);

        PDPage page = doc.getPage(0);
        try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {

            // starting y position is whole page height subtracted by top and bottom margin
            setYStartNewPage(page.getMediaBox().getHeight() - (2 * getMargin()));
            setDrawContent(true);
            setBottomMargin(70);
            setPageHeight(page.getMediaBox().getHeight());
            setCurrentPrintingHeight(getPageHeight() - 200);
            setCurrentDocument(doc);
            setCurrentPage(page);
            setCurrentContentStream(stream);

            //Print header
            drawHeader();

            // we want table across whole page width (subtracted by left and right margin ofcourse)
            setTableWidth(page.getMediaBox().getWidth() - (2 * getMargin()));

            //Print body
            drawBody();

        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        String base64 = Base64.encodeBase64String(in.readAllBytes());
        System.out.println(base64);


    }

    private void drawBody() throws Exception {
        BaseTable table = new BaseTable(getCurrentPrintingHeight(), getYStartNewPage(), getBottomMargin(), getTableWidth(), getMargin(), getCurrentDocument(), getCurrentPage(), true, isDrawContent());

        //Create its content row --one by parcel--
        Row<PDPage> row = table.createRow(12);

        Parcel pcl = new Parcel();
        pcl.setCompletionStatus("complete");
        pcl.setLabel("Hiking cap");
        pcl.setPictureURL("src/main/resources//logo.png");
        pcl.setProvidedQuantity("1");
        pcl.setReference("123456789");
        pcl.setRequiredQuantity("1");

        Cell cell1 = row.createImageCell(15, ImageUtils.readImage(new File("src/main/resources/product.png")));
        cell1 = row.createCell(25, pcl.getReference() + "<br><br>");
        cell1 = row.createCell(35, pcl.getLabel());
        cell1 = row.createCell(5, pcl.getProvidedQuantity());
        cell1 = row.createCell(5, "/" + pcl.getRequiredQuantity());
        cell1 = row.createCell(10, pcl.getCompletionStatus());

        setCurrentPrintingHeight(table.draw() - 50);
        setCurrentPage(table.getCurrentPage());
    }

    //Define the private method generating the header
    private void drawHeader() throws Exception {

        //Creating PDImageXObject object
        PDImageXObject pdImage = PDImageXObject.createFromFile("src/main/resources//logo.png", getCurrentDocument());

        //Drawing the image in the PDF document
        getCurrentContentStream().drawImage(pdImage, 150, getPageHeight() - 60, 300, 50);

        //Table for the bibox
        setTableWidth(100);
        BaseTable table = new BaseTable(getPageHeight() - 100, getYStartNewPage(), getBottomMargin(), getTableWidth(), getMargin(), getCurrentDocument(), getCurrentPage(), false, isDrawContent());
        Row<PDPage> headerRow = table.createRow(15f);
        Cell<PDPage> cell = headerRow.createCell(50, "HEADER VV");
        cell = headerRow.createCell(50, "SAMPLE");
        table.addHeaderRow(headerRow);
        table.draw();
    }

    /*
     _                              _                         
    (_)_ __  _ __   ___ _ __    ___| | __ _ ___ ___  ___  ___ 
    | | '_ \| '_ \ / _ \ '__|  / __| |/ _` / __/ __|/ _ \/ __|
    | | | | | | | |  __/ |    | (__| | (_| \__ \__ \  __/\__ \
    |_|_| |_|_| |_|\___|_|     \___|_|\__,_|___/___/\___||___/
    */
    @Data
    class Parcel {
        String pictureURL;
        String reference;
        String label;
        String providedQuantity;
        String requiredQuantity;
        String completionStatus;
    }

}
