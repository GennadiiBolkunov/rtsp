# RTSP Screen streamer

Пример Android‑приложения на Kotlin, которое запрашивает разрешение на захват экрана и транслирует его на выбранный RTSP‑сервер (например, на линукс ТВ‑приставку с плеером).

## Возможности
- Ввод хоста и порта через меню «Set RTSP endpoint». Поток публикуется по пути `/live`.
- Запрос разрешения на запись экрана через `MediaProjectionManager`.
- Трансляция экрана и звука с помощью библиотеки [`rtmp-rtsp-stream-client-java`](https://github.com/pedroSG94/rtmp-rtsp-stream-client-java).

## Сборка
1. Откройте проект в Android Studio (AGP 8.2, Kotlin 1.9).
2. Соберите и установите приложение на устройство с Android 7.0+ (minSdk=24).
3. В меню задайте адрес вида `rtsp://<host>:<port>/live`, нажмите **Start streaming** и выдайте разрешение на захват экрана.
