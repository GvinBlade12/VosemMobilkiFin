package com.example.vosemmobilkii;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button btnPosledov, btnParallel, btnToggleImage;
    ImageView dogImageView;
    boolean isImageVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPosledov = findViewById(R.id.btnPosledov);
        btnParallel = findViewById(R.id.btnParallel);
        btnToggleImage = findViewById(R.id.btnToggleImage);
        dogImageView = findViewById(R.id.dog);

        btnPosledov.setOnClickListener(v -> runPosledovTasks());
        btnParallel.setOnClickListener(v -> runParallelTasks());
        btnToggleImage.setOnClickListener(v -> toggleDogImage());
    }

    private void runPosledovTasks() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            Log.d("PROVERKASTART", "PervajaSTART");
            sleep();
            Log.d("PROVERKAFINISH", "PervajaSTOP");
        });

        executor.execute(() -> {
            Log.d("PROVERKASTART", "VtorajaSTART");
            sleep();
            Log.d("PROVERKAFINISH", "VtorajaSTOP");
        });

        executor.execute(() -> {
            Log.d("PROVERKASTART", "TretjaSTART");
            sleep();
            Log.d("PROVERKAFINISH", "TretjaSTOP");
        });

        executor.shutdown();
    }

    private void runParallelTasks() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.execute(() -> {
            Log.d("ParPROVERKASTART", "Parallel 1 START");
            sleep();
            Log.d("ParPROVERKAFINISH", "Parallel 1 STOP");
        });

        executor.execute(() -> {
            Log.d("ParPROVERKASTART", "Parallel 2 START");
            sleep();
            Log.d("ParPROVERKAFINISH", "Parallel 2 STOP");
        });

        executor.shutdown();
    }

    private void toggleDogImage() {
        if (isImageVisible) {
            dogImageView.setVisibility(View.GONE);
            isImageVisible = false;
        } else {
            fetchAndShowDogImage();
        }
    }

    private void fetchAndShowDogImage() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://random.dog/woof.json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DOG_API", "Ошибка загрузки: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("DOG_API", "Ошибка: " + response.code());
                    return;
                }

                String responseData = response.body().string();

                try {
                    JSONObject json = new JSONObject(responseData);
                    String imageUrl = json.getString("url");

                    if (imageUrl.endsWith(".mp4") || imageUrl.endsWith(".webm")) {
                        Log.w("DOG_API", "Получен не-картинка. Повтор запроса...");
                        fetchAndShowDogImage();
                        return;
                    }

                    runOnUiThread(() -> {
                        try {
                            Picasso.get().load(imageUrl).into(dogImageView);
                            dogImageView.setVisibility(View.VISIBLE);
                            isImageVisible = true;
                            Log.d("DOG_API", "Изображение загружено: " + imageUrl);
                        } catch (Exception e) {
                            Log.e("DOG_API", "Ошибка загрузки изображения: " + e.getMessage());
                        }
                    });

                } catch (Exception e) {
                    Log.e("DOG_API", "Ошибка парсинга JSON: " + e.getMessage());
                }
            }
        });
    }

    private void sleep() {
        try {
            Thread.sleep(2000); // 2 секунды
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
