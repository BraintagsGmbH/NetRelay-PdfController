/*
 * #%L
 * vertx-pojongo
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.netrelay.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.DocumentException;

import de.braintags.netrelay.NetRelayBaseTest;
import de.braintags.netrelay.controller.impl.BodyController;
import de.braintags.netrelay.controller.impl.ThymeleafTemplateController;
import de.braintags.netrelay.controller.template.Html2PdfController;
import de.braintags.netrelay.init.Settings;
import de.braintags.netrelay.routing.RouterDefinition;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.TestContext;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class THtml2PdfController extends NetRelayBaseTest {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(THtml2PdfController.class);

  @Test
  public void writeToFile1(TestContext context) throws Exception {
    writeToFile(context, "test");
  }

  @Test
  public void writeToFile2(TestContext context) throws Exception {
    writeToFile(context, "html2Pdf");
  }

  @Test
  public void writeToFile3(TestContext context) throws Exception {
    writeToFile(context, "pageBreaks");
  }

  private void writeToFile(TestContext context, String inName) throws Exception {
    File inFile = new File("testTemplates/htmlConvert/" + inName + ".html");
    context.assertTrue(inFile.exists());
    File outFile = new File("tmp/" + inName + ".pdf");
    OutputStream out = new FileOutputStream(outFile);
    InputStream in = new FileInputStream(inFile);
    createPdf(out, inFile);
    context.assertTrue(outFile.exists());
  }

  @Test
  public void useTemplateFromPath(TestContext context) {
    try {
      String url = "/htmlConvert/test.pdf";
      Buffer responseBuffer = Buffer.buffer();
      testRequest(context, HttpMethod.POST, url, req -> {
      } , resp -> {
        LOGGER.info("RESPONSE: " + resp.content);
        context.assertTrue(resp.content.contains("%PDF-"), "not a pdf");
      } , 200, "OK", null);
    } catch (Exception e) {
      context.fail(e);
    }
  }

  public static void createPdf(OutputStream out, File file) throws Exception, DocumentException, IOException {
    ITextRenderer renderer = new ITextRenderer();
    // readFonts(renderer);
    renderer.setDocument(file);
    renderer.layout();
    renderer.createPDF(out, true);
    out.close();
  }

  @Test
  public void useEmbeddedFontes(TestContext context) {
    try {
      resetRoutes("testResources");
      String url = "/htmlConvert/test.pdf";
      Buffer responseBuffer = Buffer.buffer();
      testRequest(context, HttpMethod.POST, url, req -> {
      } , resp -> {
        LOGGER.info("RESPONSE: " + resp.content);
        context.assertTrue(resp.content.contains("%PDF-"), "not a pdf");
      } , 200, "OK", null);
    } catch (Exception e) {
      context.fail(e);
    }
  }

  @Override
  protected void modifySettings(TestContext context, Settings settings) {
    super.modifySettings(context, settings);
    RouterDefinition def = defineRouterDefinition(Html2PdfController.class, "/htmlConvert/test.pdf");
    def.getHandlerProperties().put(ThymeleafTemplateController.TEMPLATE_DIRECTORY_PROPERTY, "testTemplates");
    settings.getRouterDefinitions().addAfter(BodyController.class.getSimpleName(), def);
  }

  private void resetRoutes(String fontDir) throws Exception {
    RouterDefinition def = netRelay.getSettings().getRouterDefinitions()
        .getNamedDefinition(Html2PdfController.class.getSimpleName());
    def.getHandlerProperties().put(Html2PdfController.FONT_PATH_PROP, fontDir);
    netRelay.resetRoutes();
  }

}
