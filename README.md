# Лабораторная работа №7. Сервисы и Broadcast Receivers.

# Цели
Получить практические навыки разработки сервисов (started и bound) и Broadcast Receivers.
# Выполнение работы
## Задача 1. Started сервис для скачивания изображения
В лабораторной работе №6 был разработан код, скачивающий картинку из интернета. На основе этого кода разработайте started service, скачивающий файл из интернета. URL изображения для скачивания должен передаваться в Intent. Убедитесь (и опишите доказательство в отчете), что код для скачивания исполняется не в UI потоке

Добавьте в разработанный сервис функцию отправки broadcast сообщения по завершении скачивания. Сообщение (Intent) должен содержать путь к скачанному файлу.


#### Листинг 1.1 DownloadService
```Java
public class DownloadService extends JobIntentService {

    static final int JOB_ID = 1000;
    Random random;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String url = intent.getStringExtra("URL");
        Log.d("DownloadService", Thread.currentThread().toString());
        if (url == null) {
            sendBroadcast(new Intent("BROADCAST").putExtra("MESSAGE", "path = null"));
        } else {
            String path = download(url);
            sendBroadcast(new Intent("BROADCAST").putExtra("MESSAGE", path));
        }
        stopSelf(JOB_ID);
    }

    public String download(String url) {
        String path = null;
        random = new Random();
        try {
            InputStream in = new java.net.URL(url).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            path = save(bitmap, "img" + random.nextInt(1000000));
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return path;
    }

    public String save(Bitmap bitmap, String name) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = this.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e){
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return getApplicationContext().getFileStreamPath(name).getAbsolutePath();
    }

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadService.class, JOB_ID, work);
    }
}

```

Подтверждение того, что работа ведется в фоновом потоке, можно получить с помощью логов.

Так же был изменен манифест и добавлены два сервиса(Сразу для всех заданий).
```AndroidManifest
<service
            android:name=".DownloadService"
            android:permission="android.permission.BIND_JOB_SERVICE" />
        <service
            android:name=".DownloadServiceTask3"
            android:permission="android.permission.BIND_JOB_SERVICE" />
```
Можно увидеть, что метод onClick идет в UI потоке, а наш метод onHandleWork в AsyncTask.
```Log
D/onClick(): main
D/onHandleWork: AsyncTask #1
```

## Задача 2. Broadcast Receiver
Разработайте два приложения: первое приложение содержит 1 activity с 1 кнопкой, при нажатии на которую запускается сервис по скачиванию файла. Второе приложение содержит 1 broadcast receiver и 1 activity. Broadcast receiver по получении сообщения из сервиса инициирует отображение пути к изображению в TextView в Activity.

Написал два приложения : одно по нажатию на кнопку скачивает сообщение, другое с бродкаст ресивером, по нажатию на кнопку пишет путь изображения в textView.

#### Листинг 2.1 MainActivity
```Java
public class MainActivity extends AppCompatActivity {

    Task1Binding task1Binding;
    BroadcastReceiver broadcastReceiver;
    String url = "https://pbs.twimg.com/profile_images/1045580248467886080/_uwwJdr3.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        task1Binding = Task1Binding.inflate(getLayoutInflater());
        setContentView(task1Binding.getRoot());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("MESSAGE");
                task1Binding.textView.setText(message);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("BROADCAST"));
        task1Binding.button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("thread in MainActivity", Thread.currentThread().getName());
                        DownloadService.enqueueWork(MainActivity.this,
                                new Intent(MainActivity.this,
                                        DownloadService.class).putExtra("URL", url));
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
#### Листинг 2.2 MainActivity
```Java
public class MainActivity extends AppCompatActivity {

    Task2Binding task2Binding;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        task2Binding = Task2Binding.inflate(getLayoutInflater());
        setContentView(task2Binding.getRoot());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("MESSAGE");
                task2Binding.textView.setText(message);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("BROADCAST"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
```

![alt text]( https://github.com/3oDoR/Lab7Android/blob/main/img/1.png)
![alt text]( https://github.com/3oDoR/Lab7Android/blob/main/img/2.png)

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
    Random random = new Random();

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        messenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 89) {
                    Log.d("thread", Thread.currentThread().getName());
                    new DownloadAsyncTask(msg.replyTo).execute(msg.getData().getString("URL", url));
                }
            }
        });
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        url = intent.getStringExtra("URL");
        if (url != null) {
            try {
                sendBroadcast(new Intent("BROADCAST").putExtra("MESSAGE", new DownloadAsyncTask(null).execute(url).get()));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            sendBroadcast(new Intent("BROADCAST").putExtra("MESSAGE", "path = null"));
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }


    public String download(String url) {
        String path = null;
        random = new Random();
        try {
            InputStream in = new java.net.URL(url).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            path = save(bitmap, "img" + random.nextInt(1000000));
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return path;
    }

    public String save(Bitmap bitmap, String name) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = this.getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return getApplicationContext().getFileStreamPath(name).getAbsolutePath();
    }


    class DownloadAsyncTask extends AsyncTask<String, Void, String> {
        Messenger receiver;

        DownloadAsyncTask(Messenger receiver) {
            this.receiver = receiver;
        }


        @Override
        protected String doInBackground(String... strings) {
            Log.d("doInBackground", Thread.currentThread().getName());
            return download(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Message message = Message.obtain(null, 89, "TO_ACTIVITY");
            Bundle bundle = new Bundle();
            bundle.putString("ANSWER", s);
            message.setData(bundle);
            try {
                if (receiver != null) {
                    receiver.send(message);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
```

## Листинг 3.2 MainActivity3
``` Java
public class MainActivity3 extends AppCompatActivity {
    ActivityMainBinding binding;
    BroadcastReceiver broadcastReceiver;
    Messenger boundServiceMessenger = null;
    String url = "https://pbs.twimg.com/profile_images/1045580248467886080/_uwwJdr3.jpg";


    Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 89) {
                binding.textView.setText(msg.getData().getString("ANSWER"));
            }
        }
    });

    private final ServiceConnection ServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("MainActivity", "Service connected");
            boundServiceMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MainActivity", "Service disconnected");
            boundServiceMessenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.button.setOnClickListener(
                v -> {
                    Message message = Message.obtain(null, 89, "TO_SERVICE");
                    Bundle bundle = new Bundle();
                    message.replyTo = messenger;
                    bundle.putString("URL", url);
                    message.setData(bundle);
                    try {
                        boundServiceMessenger.send(message);
                    } catch (RemoteException e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("MESSAGE");
                binding.textView.setText(message);
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("BROADCAST"));
        binding.button2.setOnClickListener(
                v -> {
                    Log.d("thread in MainActivity", Thread.currentThread().getName());
                    startService(new Intent(MainActivity3.this, DownloadServiceTask3.class).putExtra("URL", url));
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity3.this, DownloadServiceTask3.class);
        bindService(intent, ServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
```
![alt text](https://github.com/3oDoR/Lab7Android/blob/main/img/3.png "task3" )

## Выводы
В этот раз я не засекал время, поэтому скажу только примерное время выполнения заданий.
Задание 1 - 6-8 часов - Оказалось весьма простое, но  пришлось разбираться. Основное время ушло на 
просмотр лекций по этой теме и изучение предложеных материалов, что значительно сократило время 
последующих задач.
Задание 2 - 2-3 часа - очень простое, потому что мы в первой задаче уже сделали всё необходимое. 
Здесь нужно было лишь "взять-отправить".
Задание 3 - 6-8 часов - долго разбирался что-кому-куда передается.
В ходе выполнения работы были получены опыт в разработке сервисов.Изучены дополнительные классы,
 интерфейсы и абстрактные классы, как, например: Message, Messenger, Binder.
