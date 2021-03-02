# Лабораторная работа №7. Сервисы и Broadcast Receivers.

# Цели
Получить практические навыки разработки сервисов (started и bound) и Broadcast Receivers.
# Выполнение работы
## Задача 1. Started сервис для скачивания изображения
В лабораторной работе №6 был разработан код, скачивающий картинку из интернета. На основе этого кода разработайте started service, скачивающий файл из интернета. URL изображения для скачивания должен передаваться в Intent. Убедитесь (и опишите доказательство в отчете), что код для скачивания исполняется не в UI потоке

Добавьте в разработанный сервис функцию отправки broadcast сообщения по завершении скачивания. Сообщение (Intent) должен содержать путь к скачанному файлу.


Обработка url происходит в методе onHandleWork, если не передан url посылаем в бродкаст сообщение об ошибке, либо скачиваем изображение.Если url не null, то вызываем метод download основоный на коде из пролой работы. Метод save сохраняет файл. 
#### Листинг 1.1 DownloadService
```Java
public class DownloadService extends JobIntentService {

    static final int JOB_ID = 2;

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d("thread In DownloadService", Thread.currentThread().getName());
        String url = intent.getStringExtra("url");
        if (url == null) {
            sendBroadcast(new Intent("broadcast").putExtra("Message", "path = null"));
            stopSelf();
        } else {
            try {
                String path = download(url);
                sendBroadcast(new Intent("broadcast").putExtra("Message", path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String download(String url) throws IOException {
        Bitmap bitmap;
        String path = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
            path = save(bitmap, "img" + (int) (Math.random() * 10000));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return path;
    }

    public String save(Bitmap bitmap, String name) {
        FileOutputStream Stream;
        try {
            Stream = this.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, Stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return getApplicationContext().getFileStreamPath(name).getAbsolutePath();
    }

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadService.class, JOB_ID, work);
    }
}
```

Добавления в манивест
#### Листинг 1.2 AndroidManifest
```xml
<uses-permission android:name="android.permission.INTERNET" />
...
  <service
            android:name=".DownloadService"
            android:permission="android.permission.BIND_JOB_SERVICE" />
```

## Задача 2. Broadcast Receiver
Разработайте два приложения: первое приложение содержит 1 activity с 1 кнопкой, при нажатии на которую запускается сервис по скачиванию файла. Второе приложение содержит 1 broadcast receiver и 1 activity. Broadcast receiver по получении сообщения из сервиса инициирует отображение пути к изображению в TextView в Activity.

Написал два приложения : одно по нажатию на кнопку скачивает сообщение, другое с бродкаст ресивером, по нажатию на кнопку пишет путь изображения в textView.

#### Листинг 2.1 MainActivity
```Java
public class MainActivity extends AppCompatActivity {

    Task1Binding task1Binding;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        task1Binding = Task1Binding.inflate(getLayoutInflater());
        setContentView(task1Binding.getRoot());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message= intent.getStringExtra("Message");
                task1Binding.textView.setText(message);
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("broadcast"));
        task1Binding.button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("thread in MainActivity", Thread.currentThread().getName());
                        Intent intent = new Intent(MainActivity.this,
                                DownloadService.class).putExtra("url",
                                "https://mymeizu.md/wp-content/uploads/" +
                                        "2016/11/servisy-google-meizu.jpg");
                        DownloadService.enqueueWork(MainActivity.this,intent);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
```
#### Листинг 2.2 MainActivity2
```Java
public class MainActivity2 extends AppCompatActivity {
    Task2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = Task2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("thread in MainActivity", Thread.currentThread().getName());
                        Intent intent = new Intent(MainActivity2.this, DownloadService.class).putExtra("url", "https://mymeizu.md/wp-content/uploads/2016/11/servisy-google-meizu.jpg");
                        DownloadService.enqueueWork(MainActivity2.this,intent);
                    }
                });
    }
}
```

![alt text]( https://github.com/3oDoR/Lab7Android/blob/main/img/1.png "task2_1")
![alt text]( https://github.com/3oDoR/Lab7Android/blob/main/img/2.png "task2_2")

## Задача 3. Bound Service для скачивания изображения
Сделайте разработанный сервис одновременно bound И started: переопределите метод onBind. Из тела метода возвращайте IBinder, полученный из класса Messenger. Убедитесь (доказательство опишите в отчете), что код скачивания файла исполняется не в UI потоке.

Измените способ запуска сервиса в первом приложении: вместо startService используйте bindService. При нажатии на кнопку отправляйте сообщение Message, используя класс Messenger, полученный из интерфейса IBinder в методе onServiceConnected.

Добавьте в первое приложение TextView, а в сервис отправку обратного сообщения с местоположением скачанного файла. При получении сообщения от сервиса приложение должно отобразить путь к файлу на экране.

Обратите внимание, что разработанный сервис должен быть одновременно bound И started. Если получен интент через механизм started service, то сервис скачивает файл и отправляет broadcast (started service не знает своих клиентов и не предназначен для двухсторонней коммуникации). Если получен message через механизм bound service, то скачивается файл и результат отправляется тому клиенту, который запросил этот файл (т.к. bound service знает всех своих клиентов и может им отвечать).


## Листинг 3.1 DownloadServiceTask3
```Java
public class DownloadServiceTask3 extends Service {
    
    String url;
    Messenger messenger;

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        messenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    new DownloadAsyncTask(msg.replyTo).execute(msg.getData().getString("url", url));
                }
                super.handleMessage(msg);
            }
        });
        return messenger.getBinder();
    }

    class DownloadAsyncTask extends AsyncTask<String, Void, String> {

        private Messenger receiver;

        DownloadAsyncTask(Messenger receiver) {
            this.receiver = receiver;
        }

        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... strings) {
            Log.d("thread In DownloadService", Thread.currentThread().getName());
            String urls = strings[0];
            String path = null;
            try {
                path = download(urls);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return path;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Message message = Message.obtain(null, 1);
            Bundle data = new Bundle();
            data.putString("answer", s);
            message.setData(data);
            try {
                if (receiver != null) {
                    receiver.send(message);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        if (url == null) {
            sendBroadcast("path = null");
            stopSelf(startId);
        } else {
            try {
                sendBroadcast(new DownloadAsyncTask(null).execute(url).get());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;
    }

    public String download(String url) throws IOException {
        Bitmap mIcon11 = null;
        String path = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
            path = save(mIcon11, "img" + (int) (Math.random() * 10000));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return path;
    }

    public String save(Bitmap bitmap, String name) {
        FileOutputStream Stream;
        try {
            Stream = this.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, Stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return getApplicationContext().getFileStreamPath(name).getAbsolutePath();
    }

    public void sendBroadcast(String message) {
        sendBroadcast(new Intent("broadcast").putExtra("Message", message));
    }
}
```

## Листинг 3.2 MainActivity3
``` Java
public class MainActivity3 extends AppCompatActivity {
    ActivityMainBinding binding;
    BroadcastReceiver br;
    Messenger boundServiceMessenger = null;
    private Boolean connected = false;
    Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                binding.textView.setText(msg.getData().getString(" "));
            }
            super.handleMessage(msg);
        }
    });

    private ServiceConnection ServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundServiceMessenger = new Messenger(service);
            connected = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundServiceMessenger = null;
            connected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message message = Message.obtain(null, 1);
                        Bundle data = new Bundle();
                        data.putString("url", "https://mymeizu.md/wp-content/uploads/2016" +
                                "/11/servisy-google-meizu.jpg");
                        message.replyTo = messenger;
                        message.setData(data);
                        try {
                            boundServiceMessenger.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("Message");
                binding.textView.setText(message);
            }
        };

        registerReceiver(br, new IntentFilter("broadcast"));
        binding.button2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("thread in MainActivity", Thread.currentThread().getName());
                        Intent intent = new Intent(MainActivity3.this,
                                DownloadServiceTask3.class).putExtra("url",
                                "https://mymeizu.md/wp-content/uploads/2016/" +
                                        "11/servisy-google-meizu.jpg");
                        startService(intent);
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity3.this, DownloadServiceTask3.class);
        bindService(intent, ServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connected) {
            unbindService(ServiceConnection);
            connected = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }
}
```
![alt text](https://github.com/3oDoR/Lab7Android/blob/main/img/3.png "task3_1" )
![alt text](https://github.com/3oDoR/Lab7Android/blob/main/img/4.png "task3_2" )
## Выводы
Задание 1 - 5 часа - весьма простое, потому что ты практически полностью копипастишь код из 6 работы. Основное время ушло на просмотр лекции и изучение предложеных материалов, что значительно сократило время последующих задач.
Задание 2 - 2 часа - очень простое, потому что мы в первой задаче уже сделали всё необходимое. Здесь нужно было лишь "взять-отправить".
Задание 3 - 4 часа - долго разбирался что-кому-куда передается, чтобы брать это и использовать. 

