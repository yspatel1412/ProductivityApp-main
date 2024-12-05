package com.yash.productivityapp;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SplitPdfActivity extends AppCompatActivity {

    private TextView uploadStatusTextView;
    private LinearLayout pdfContainerLayout;
    private Button downloadPdfButton;
    private Uri selectedPdfUri;
    private int splitPage; // Page number to split
    private List<Bitmap> renderedPages; // To store the rendered pages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_pdf);

        uploadStatusTextView = findViewById(R.id.uploadStatusTextView);
        pdfContainerLayout = findViewById(R.id.pdfContainerLayout);
        downloadPdfButton = findViewById(R.id.downloadPdfButton);

        renderedPages = new ArrayList<>();

        // Retrieve the URI of the selected PDF and the split page from intent
        String pdfUriString = getIntent().getStringExtra("pdfUri");
        splitPage = getIntent().getIntExtra("splitPage", 1); // Default to 1 page if not specified

        if (pdfUriString != null) {
            selectedPdfUri = Uri.parse(pdfUriString);
            renderPdf(selectedPdfUri, splitPage);
        } else {
            uploadStatusTextView.setText("Invalid PDF URI");
        }

        // Set up the download button
        downloadPdfButton.setOnClickListener(v -> {
            if (!renderedPages.isEmpty()) {
                try {
                    savePdfToFile(renderedPages);
                    Toast.makeText(SplitPdfActivity.this, "PDF downloaded successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SplitPdfActivity.this, "Error downloading PDF", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SplitPdfActivity.this, "No pages to download", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Render the PDF pages from 1 to splitPage
    private void renderPdf(Uri fileUri, int pageCountToRender) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Open the PDF file
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(fileUri, "r");
                if (parcelFileDescriptor != null) {
                    PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                    int totalPageCount = pdfRenderer.getPageCount();

                    // Ensure that we do not exceed the total page count
                    int pagesToRender = Math.min(pageCountToRender, totalPageCount);

                    // Loop through all pages from 1 to the requested number of pages
                    for (int i = 0; i < pagesToRender; i++) {
                        PdfRenderer.Page page = pdfRenderer.openPage(i);

                        // Create a Bitmap to render the page to
                        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                        // Store the Bitmap for later PDF creation
                        renderedPages.add(bitmap);

                        // Create an ImageView to hold the rendered page
                        ImageView pageImageView = new ImageView(this);
                        pageImageView.setImageBitmap(bitmap);

                        // Add the ImageView to the container (which is scrollable)
                        pdfContainerLayout.addView(pageImageView);

                        // Close the page after rendering
                        page.close();
                    }

                    pdfRenderer.close();
                    uploadStatusTextView.setText("PDF pages rendered successfully.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                uploadStatusTextView.setText("Error rendering PDF: " + e.getMessage());
            }
        }
    }

    // Method to save the rendered pages as a new PDF
    private void savePdfToFile(List<Bitmap> pages) throws IOException {
        PdfDocument pdfDocument = new PdfDocument();

        // Loop through the list of rendered pages
        for (int i = 0; i < pages.size(); i++) {
            Bitmap pageBitmap = pages.get(i);

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageBitmap.getWidth(), pageBitmap.getHeight(), i + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            // Draw the Bitmap onto the page
            page.getCanvas().drawBitmap(pageBitmap, 0, 0, null);

            // Finish the page
            pdfDocument.finishPage(page);
        }

        // Create the output file (in app's private storage or external storage)
        File outputDir = new File(getExternalFilesDir(null), "splitted_pdfs");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File(outputDir, "split_pages.pdf");
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            pdfDocument.writeTo(fileOutputStream);
        }

        // Close the PDF document
        pdfDocument.close();

        Toast.makeText(this, "File saved to: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }
}
