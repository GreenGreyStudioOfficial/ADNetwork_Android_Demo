
[Manual](#manual) | [API](#api)
_____
# API

# Статичный класс AdvSDK
Статичный класс **AdvSDK** - публичный интерфейс для взаимодействия с **SDK**.

Включает в себя следующие публичные методы:

- [Initialize](#initialize): инициализации работы **SDK**;
- [Load](#api_load): загрузка доступного рекламного объявления из сети или из кэша;
- [Show](#api_show): показ загруженного рекламного объявления.

## Содержание

- [Метод Initialize](#initialize)
- [Метод Load](#api_load)
- [Метод Show](#api_show)
- [Слушатели](#listeners)
- [Слушатель инициализации](#l_initialization)
- [Слушатель загрузки](#l_load)
- [Слушатель показа](#l_show)
- [Модель объекта AdNetworkInitParams](#AdNetworkInitParams)

## Метод Initialize <a name = "initialize"></a>

Метод **Initialize** инициализирует работу **SDK**.

На вход передаются параметры инициализации **SDK** и реализация слушателя [IAdInitializationListener](#IAdInitializationListener)

Без инициализации методы [AdvSDK.load](#api_load) и [AdvSDK.show](#api_show) не отработают корректно и будут сообщать своим слушателям об ошибках
**[LoadErrorType.NOT_INITIALIZED_ERROR](#errors_explanation)** и **[ShowErrorType.NOT_INITIALIZED_ERROR](#errors_explanation)** соответственно.

Если инициализация запущена, но не завершена, методы [AdvSDK.load](#api_load) и [AdvSDK.show](#api_show) не отработают корректно и будут сообщать своим слушателям об ошибках
**[LoadErrorType.INITIALIZATION_NOT_FINISHED](#errors_explanation)** и **[ShowErrorType.INITIALIZATION_NOT_FINISHED](#errors_explanation)**
соответственно.

Если **SDK** успешно инициализирован, то при попытке повторной инициализации, вызовется **callback** ее слушателя [IAdInitializationListener.OnInitializationError](#OnInitializationError) с ошибкой **[InitializationErrorType.SDK_ALREADY_INITIALIZED](#errors_explanation)**.

Если при инициализации был передан некорректный **GAME_ID** (null, "", invalid), то будет вызван **callback** слушателя [IAdInitializationListener.OnInitializationError](#OnInitializationError) с ошибкой **[InitializationErrorType.GAME_ID_IS_NULL_OR_EMPTY](#errors_explanation)**.


**Объявление**:

```Kotlin
fun initialize(  
    context: Application,  
    gameId: String,  
    isTestMode: Boolean,  
    listener: IAdInitializationListener  
)
```

где:

| Тип| Имя| Описание|
|---|---|---|
|Application| context| Контекст приложения|
|String| gameId| **GAME_ID** - идентификатор приложения в системе показа рекламы. |
|[IAdInitializationListener](#IAdInitializationListener)| listener| Реализация слушателя инициализации|

## Метод Load <a name = "api_load"></a>

Метод **Load** загружает доступное рекламное объявление из сети или из кеша.

На вход нужно передать [тип рекламного объявления](#adtype), реализацию слушателя [IAdLoadListener](#IAdLoadListener).

Метод запускает процесс загрузки рекламных объявлений. По завершению процесса загрузки вызовется **callback** слушателя с тем же типом рекламного объявления, с которым был вызван метод **Load**.

Метод сохраняет данные рекламного объявления в кэш, который самостоятельно очищается после показа рекламы, либо по истечению срока годности ролика.

**Объявление**:

```Kotlin
fun load(advertiseType: AdvertiseType, listener: IAdLoadListener)
```

где:

`AdvertiseType` - тип рекламного объявления (см. [AdvertiseType](#adtype));

`IAdLoadListener` - реализация слушателя загрузки (см. [IAdLoadListener](#IAdLoadListener));

## Метод Show <a name = "api_show"></a>

Показывает загруженное рекламное объявление.

На вход нужно передать [тип рекламного объявления](#adtype), реализацию слушателя [IAdShowListener](#l_show) и **placementId** рекламного креатива.

Метод запускает процесс показа ролика.

Перед показом вызывается **callback** [IAdShowListener.OnShowStart](#OnShowStart)

По окончанию успешного показа у слушателя вызовется **callback** [IAdShowListener.OnShowComplete](#OnShowComplete), сообщив о статусе завершения.

Это важно, если, например, объявление имело тип `AdType.REWARDED`. Так как в этом случае для присуждения награды нужно знать был ли ролик досмотрен до конца (`ShowCompletionState.SHOW_COMPLETE_BY_CLOSE_BUTTON`) или пропущен (`ShowCompletionState.SHOW_COMPLETE_BY_SKIP_BUTTON`).

В случае, если во время показа произошла ошибка, будет вызван **callback** [IAdShowListener.OnShowError](#OnShowError).
Так, например, если срок кэша ролика истек до вызова, то на вход методу передастся ошибка `ShowErrorType.VIDEO_WAS_DELETED`.

После завершения показа, вне зависимости от его успешности, кэш объявления очищается.

**Объявление**:

```Kotlin
fun show(id: String, iAdShowListener: IAdShowListener)
```

где:

`id` - плейсмент ИД рекламного объявления.

`IAdShowListener` - реализация слушателя показа (см. [IAdShowListener](#l_show));



# Слушатели <a name = "listeners"></a>

Слушатели (или **listeners**) - это интерфейсы, которые дают возможность контролировать процессы инициализации, загрузки и показа рекламных объявлений.

В системе используется три вида слушателей:

- [Слушатель инициализации IAdInitializationListener](#l_initialization);
- [Слушатель загрузки IAdLoadListener](#l_load);
- [Слушатель показа IAdShowListener](#l_show).

## Слушатель инициализации <a name = "l_initialization"></a>

Интерфейс слушателя инициализации **SDK** **IAdInitializationListener** используется для контроля выполнения процесса инициализации.

Использует следующие публичные методы:

- [onInitializationComplete](#OnInitializationComplete): обработчик завершения инициализации;
- [onInitializationError](#OnInitializationError): обработчик ошибок инициализации;

### onInitializationComplete <a name = "OnInitializationComplete"></a>

Обработчик завершения инициализации вызывается, когда инициализация прошла успешно.

**Объявление**:

```Kotlin
fun OnInitializationComplete();
```

### onInitializationError <a name = "OnInitializationError"></a>

Обработчик ошибок завершения инициализации вызывается, когда инициализация прошла с ошибкой.

**Объявление**:

```Kotlin
fun onInitializationError(error: InitializationErrorType, errorMessage: String)
```

| Тип| Имя| Описание|
|---| --- |---|
|InitializationErrorType| error| Тип ошибки|
|string| errorMessage| Информация об ошибке|

**Варианты ошибок**: <a name = "errors_explanation"></a>

| Значение| Описание| 
|---|---|
| UNKNOWN| Неизвестная ошибка|
|GAME_ID_IS_NULL_OR_EMPTY | Задан пустой идентификатор игры| 
| SDK_ALREADY_INITIALIZED| Инициализация уже была проведена|


## Слушатель загрузки

Интерфейс слушателя загрузки **IAdLoadListener** используется для контроля выполнения процесса загрузки.

Использует следующие публичные методы:

- [onLoadComplete](#OnLoadComplete) - обработчик завершения загрузки;
- [onLoadError](#OnLoadError) - обработчик ошибок загрузки рекламных объявлений.

### onLoadComplete <a name = "OnLoadComplete"></a> 

Обработчик завершения загрузки вызывается, когда загрузка прошла успешно.

**Объявление**:

```Kotlin
fun onLoadComplete(id: String)
```

где: 

| Тип| Имя| Описание|
|---|---|---|
| String| id|  плейсмент ИД рекламного объявления|

### onLoadError <a name = "OnLoadError"></a> 

Обработчик ошибок загрузки вызывается, когда загрузка прошла с ошибкой.

**Объявление**:

```Kotlin
fun onLoadError(error: LoadErrorType, errorMessage: String, id: String)
```

где: 

| Тип| Имя| Описание|
|---|---|---|
| String| id| плейсмент ИД рекламного объявления|
|LoadErrorType | error| Тип ошибки|
|string | errorMessage| Сообщение об ошибке|

**Варианты типа ошибки**:

| Значение| Описание|
|---|---|
|UNKNOWN| Неизвестная ошибка|
| CONNECTION_ERROR| Ошибка соединения| 
| DATA_PROCESSING_ERROR| Ошибка обработки данных|
| PROTOCOL_ERROR| Ошибка протокола|
| NOT_INITIALIZED_ERROR| Отсутствие инициализации|
| TO_MANY_VIDEOS_LOADED|  Превышен лимит загрузки видео данного типа|
| AVAILABLE_VIDEO_NOT_FOUND| Сервис предоставления рекламы не нашел соответствующий ролик|
| NO_CONTENT| Нет контента|
| WEBVIEW_CONTENT_NOT_LOADED| Ошибка загрузки WebView|


## Слушатель показа <a name = "l_show"></a>

Интерфейс слушателя показа рекламного объявления **IAdShowListener** используется для контроля выполнения процесса показа.

Поддерживает следующие публичные методы:

- [[#onShowChangeState]] - обработчик завершения показа объявления;
- [onShowError](#onShowError) - обработчик ошибок показа рекламного объявления.

### onShowChangeState <a name = "OnShowStart"></a> 

Обработчик вызывается при изменении состояния пока ролика.

**Объявление**:

```C#
fun onShowChangeState(id: String, state: ShowCompletionState)
```

где:

|Тип| Имя| Описание|
|---|---|---|
| String| id| плейсмент ИД рекламного объявления|
| ShowCompletionState| state|  Статус показа рекламы|

**Варианты статуса показа**:

| Значение| Описание | 
|---|---|
| START| Ролик начался|
| CLOSE| Завершение показа по кнопке **Закрыть**|
| SKIP| Завершение показа по кнопке **Пропустить**|
| COMPLETECOMPLETE| Ролик завершился|

### onShowError <a name = "OnShowError"></a>  

Обработчик ошибок показа рекламного объявления вызывается, когда показ прошел с ошибкой.

**Объявление**:

```Kotlin
fun onShowError(id: String, error: ShowErrorType, errorMessage: String)
```

где:

|Тип| Имя| Описание| 
|---|---|---|
| String| id| плейсмент ИД рекламного объявления|
| ShowErrorType | error| Тип ошибки|
| string | errorMessage| описание ошибки|

**Варианты ошибки показа**:

| Значение| Описание| 
|---|---|
| UNKNOWN| Неизвестная ошибка|
| NOT_INITIALIZED_ERROR| Отсутствует инициализация| 
|VIDEO_PLAYER_ERROR| Ошибка видеоплеера|
|NO_LOADED_CONTENT| Нет загруженного контента|
| NOT_SUPPORTED_AD_TYPE| Неподдерживаемый тип рекламного объявления|
