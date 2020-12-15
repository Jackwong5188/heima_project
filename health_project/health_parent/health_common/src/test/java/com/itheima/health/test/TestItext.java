package com.itheima.health.test;

import org.junit.Test;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.FileOutputStream;

public class TestItext {
    // 入门案例
    @Test
    public void createPdf() throws Exception {
        // 1：创建一个文档对象
        Document document = new Document();
        // 2：获取1个PdfWriter对象实例
        PdfWriter.getInstance(document, new FileOutputStream("E:/itext86.pdf"));
        // 解决中文问题
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font font = new Font(bfChinese,12, Font.NORMAL, Color.RED);
        // 3：打开文档（方便添加数据）
        document.open();
        // 4：添加数据
        document.add(new Paragraph("Hello World! 你好! ^_^",font));
        // 5：关闭文档
        document.close();
    }
}
