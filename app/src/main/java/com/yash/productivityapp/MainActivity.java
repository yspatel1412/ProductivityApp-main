package com.yash.productivityapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView filePathTextView, uploadStatusTextView;
    private LinearLayout pdfContainerLayout;  // Container to hold rendered pages

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filePathTextView = findViewById(R.id.filePathTextView);
        uploadStatusTextView = findViewById(R.id.uploadStatusTextView);
        pdfContainerLayout = findViewById(R.id.pdfContainerLayout);  // Layout to hold rendered pages

        Button uploadFileButton = findViewById(R.id.uploadFileButton);

        // Open file picker when the upload button is clicked
        uploadFileButton.setOnClickListener(v -> openFilePicker());
    }

    // Open file picker
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf"); // Restrict file picker to PDFs
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                filePathTextView.setText("File Selected: " + fileUri.toString());
                renderPdf(fileUri);
            }
        }
    }

    // Render all pages of the PDF
    private void renderPdf(Uri fileUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Open the PDF file
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(fileUri, "r");
                if (parcelFileDescriptor != null) {
                    PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                    int pageCount = pdfRenderer.getPageCount();

                    // Loop through all pages and render them
                    for (int i = 0; i < pageCount; i++) {
                        PdfRenderer.Page page = pdfRenderer.openPage(i);

                        // Create a Bitmap to render the page to
                        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                        // Create an ImageView to hold the rendered page
                        ImageView pageImageView = new ImageView(this);
                        pageImageView.setImageBitmap(bitmap);

                        // Add the ImageView to the container (which is scrollable)
                        pdfContainerLayout.addView(pageImageView);

                        // Close the page after rendering
                        page.close();
                    }

                    pdfRenderer.close();
                    uploadStatusTextView.setText("PDF rendered successfully");
                }
            } catch (IOException e) {
                e.printStackTrace();
                uploadStatusTextView.setText("Error rendering PDF: " + e.getMessage());
            }
        }
    }
}
