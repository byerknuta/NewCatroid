package org.catrobat.catroid.content;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.actions.CreateCubeAction;
import org.catrobat.catroid.content.actions.PlaySoundAction;
import org.catrobat.catroid.content.actions.SetMassAction;
import org.catrobat.catroid.content.actions.TurnRightSpeedAction;
import org.catrobat.catroid.content.actions.VmRelativeMouseMoveAction;
import org.catrobat.catroid.content.bricks.*;

import java.util.HashMap;
import java.util.Map;

public final class BrickInfo {
    private static final String DESC_NOT_FOUND = "Описание для этого блока еще не добавлено.";

    private static final Map<Class<? extends Brick>, String> brickDescriptions = new HashMap<>();

    static {
        add(SetGeminiKeyBrick.class, "Задает персональный API ключ Gemini. Получить его можно в Google AI Studio.");
        add(AskGeminiBrick.class, "Задает вопрос к Gemini (gemini-flash-lite-latest). ВАЖНО: не ждет ответа, код выполняется дальше.");
        add(AskGemini2Brick.class, "Задает вопрос к определенной модели Gemini. ВАЖНО: не ждет ответа, код выполняется дальше.");
        add(AskGPTBrick.class, "Задает вопрос к ChatGPT (через Pollinations.Ai).");
        add(CreateFloatBrick.class, "Создает FloatArray в памяти устройства.");
        add(PutFloatBrick.class, "Вставляет значение во FloatArray.");
        add(DeleteFloatBrick.class, "Удаляет элемент из FloatArray.");
        add(TableToFloatBrick.class, "Добавляет всю таблицу во FloatArray.");
        add(LoadNNBrick.class, "Загружает .onnx модель нейросети. ВАЖНО: во избежании утечек памяти - выгружайте ее при выходе.");
        add(PredictNNBrick.class, "Принимает на вход FloatArray соответствующий размерам тензора, который принимает модель. Возвращает все значения выходного слоя и самое большое из них.");
        add(UnloadNNBrick.class, "Выгружает модель, освобождая память. ВАЖНО: вызывайте после загрузки модели, когда она уже не нужна.");
        add(ResizeImgBrick.class, "Изменяет размер изображения из файла и сохраняет в этот же файл.");
        add(GrayscaleImgBrick.class, "Делает изображение черно-белым и сохраняет в этот же файл.");
        add(NormalizeImgBrick.class, "Нормализует изображение в диапозон от 0 до 1 и сохраняет каждый канал в таблицу.");
        add(CutLookBrick.class, "Обрезает образие спрайта.");
        add(WhenStartedBrick.class, "Вызывается при запуске сцены 1 раз.");
        add(WhenBrick.class, "Вызывается при нажатии пальца на спрайт.");
        add(WhenTouchDownBrick.class, "Вызывается, когда палец касается экрана.");
        add(BroadcastReceiverBrick.class, "Вызывается, когда любой спрайт в этой сцене отправит выбранное сообщение.");
        add(WhenProjectExitsBrick.class, "Вызывается, когда пользователь выходит из проекта. Можно использовать для выгрузки данных или сохранения.");
        add(WhenBackPressedBrick.class, "Вызывается, когда пользователь нажимает на кнопку \"назад\" на устройстве.");
        add(WhenBeforeUpdateBrick.class, "Вызывается все время ПЕРЕД ОТРИСОВКОЙ! используйте для синхронизации логики и рендера");
        add(WhenBeforeUpdateBrick.class, "Вызывается все время ПОСЛЕ ОТРИСОВКИ!");
        add(WhenConditionBrick.class, "Вызывается, как только условие в станет истиной.");
        add(WhenBackgroundChangesBrick.class, "Вызывается, когда фон изменит образ на выбранный.");
        add(WhenClonedBrick.class, "Вызывается ТОЛЬКО для клона, сразу после его клорования.");
        add(CloneBrick.class, "Клонирует текущий спрайт. ВАЖНО: включая все его переменные, трансформации и образы.");
        add(CloneAndNameBrick.class, "Клонирует текущий спрайт и задает ему имя. ВАЖНО: копирует все его переменные, трансформации и образы.");
        add(DeleteThisCloneBrick.class, "Удаляет текущий клон. ВАЖНО: только для клона, не для оригинального спрайта.");
        add(NoteBrick.class, "Ничего не делает :/ используйте его для своих заметок.");
        add(ForeverBrick.class, "Вечно повторяет блоки внутри, пока его что-нибудь не остановит.");
        add(SetLookBrick.class, "Изменяет внешний вид спрайта на выбранный из списка.");
        add(WaitBrick.class, "Приостанавливает выполнение на определенное время. ВАЖНО: игнорируется в асинхронном цикле");
        add(IfThenLogicBeginBrick.class, "Выполняет блоки внутри, если условие истинно.");
        add(IfLogicElseBrick.class, "Выполняет блоки внутри, если условие ложь.");
        add(IfThenLogicEndBrick.class, "Конец блока \"Если\"");
        add(AsyncRepeatBrick.class, "Повторяет блоки без задержек. ВАЖНО: плохо работает. игнорирует блоки \"Ждать\"");
        add(InstantBrick.class, "Выполняет все блоки внутри без задержек. ВАЖНО: может вызвать фризы при тяжелых действиях");
        add(SpawnThreadBrick.class, "Запускает блоки в новом потоке с ID (не ждет окончания!). Используйте для многопоточности!");
        add(WaitThreadBrick.class, "Ждет окончания выполнения блоков внутри потока с ID");
        add(StopThreadBrick.class, "Принудительно останавливает выполнение блоков в потоке с ID");
        add(IntervalRepeatBrick.class, "Повторяет блоки с определенным интервалом");
        add(WaitUntilBrick.class, "Приостанавливает выполнение, пока условие не станет истинным");
        add(RepeatBrick.class, "Повторяет блоки несколько раз");
        add(RunAsSpriteBrick.class, "Запускает блоки внутри от имени определенного спрайта");
        add(RepeatUntilBrick.class, "Повторяет блоки, пока условие ложно");
        add(ForItemInUserListBrick.class, "Повторяет блоки для каждого элемента в списке. в переменную записывается значение текущего элемента");
        add(ForVariableFromToBrick.class, "Повторяет блоки для каждого значения в диапозоне. в переменную записывается текущее значение.");
        add(SceneTransitionBrick.class, "Продолжает сцену с того момента, на котором она закончилась. ВАЖНО: не работает, если сохранение сцен выключено");
        add(SceneStartBrick.class, "Запускает сцену");
        add(SceneIdBrick.class, "Запускает сцену под ее номером");
        add(ClearSceneBrick.class, "Очищает кэш сцены ВАЖНО: после этого ее нельзя будет продолжить.");
        add(SetSaveScenesBrick.class, "Определяет сохранять ли сцены.");
        add(SetStopSoundsBrick.class, "Определяет остановку звуков, при переходе на другую сцену");
        add(ReturnToPreviousProjectBrick.class, "Возвращается к предыдущему проекту, откуда был запущен этот ВАЖНО: плохо работает");
        add(LaunchProjectBrick.class, "Запускает проект из файлов проекта, останавливает текущий");
        add(FinishStageBrick.class, "Выходит из проекта, или приложения (в APK)");
        add(StopScriptBrick.class, "Останавливает выбранный скрипт (-ы)");
        add(WaitTillIdleBrick.class, "Ждет, пока во всей сцене остановятся все скрипты");
        add(TryCatchFinallyBrick.class, "Пробует выполнить действие, при фатальной ошибке - перехватывает ее. В конце вызывает блоки");
        add(BroadcastBrick.class, "Отправляет выбранное сообщение");
        add(BroadcastWaitBrick.class, "Отправляет выбранное сообщение и ждет, пока все получатели завершат действия");
        add(PlaceAtBrick.class, "Задает позицию спрайту");
        add(SetXBrick.class, "Задает позицию X спрайту");
        add(SetYBrick.class, "Задает позицию Y спрайту");
        add(ChangeXByNBrick.class, "Изменяет позицию X спрайта на определенное значение");
        add(ChangeYByNBrick.class, "Изменяет позицию Y спрайта на определенное значение");
        add(GoToBrick.class, "Помещает спрайт на позицию");
        add(SetShaderCodeBrick.class, "Задает вершинный и фрагментный шейдер. Гайды можно найти в интернете (ищите LibGDX, GLSL). ВАЖНО: ЕСЛИ ВЫ НЕ ЗНАЕТЕ КАК ИМИ ПОЛЬЗОВАТЬСЯ - НЕ ПОЛЬЗУЙТЕСЬ. В СЛУЧАЕ ГРАФИЧЕСКИХ БАГОВ - ПОПРОБУЙТЕ ОТКЛЮЧИТЬ ИЛИ УДАЛИТЬ ЭТОТ БЛОК!");
        add(Set3dFrictionBrick.class, "Задает трение обьекту ВАЖНО: ЕСТЬ СПЕЦ. ФУНКЦЯ: если указать трение > 10 000, то будет задаваться затухание. например трение: 10 001.5 даст затухание 1.5, а 10 000 даст 0");
        add(MoveNStepsBrick.class, "Перемещает спрайт на определенное количество шагов вперед по направлению");
        add(TurnRightBrick.class, "Поворачивает спрайт вправо на определенное количество градусов");
        add(TurnLeftBrick.class, "Поворачивает спрайт влево на определенное количество градусов");
        add(PointInDirectionBrick.class, "Задает направление спрайту");
        add(PointToBrick.class, "Поворачивает спрайт к другому спрайту");
        add(SetRotationStyleBrick.class, "Задает тип вращения.");
        add(GlideToBrick.class, "Плавно перемещает спрайт к позиции");
        add(CreateRevoluteJointBrick.class, "Создает шарнир для 2-х спрайтов со смещением");
        add(CreatePulleyJointBrick.class, "Создает шарнир для 2-х спрайтов, который можно представить как лифт с противовесом\nТочка опоры А             Точка опоры Б\n" +
                "             (gA)---------------------(gB)\n" +
                "              |                        |\n" +
                "              |                        |\n" +
                "            [Спрайт А]               [Спрайт Б]");
        add(CreateGearJointBrick.class, "Создает шарнир для 2-х других шарниров, можно представить как шестерни. 1 шарнир будет вращаться в зависимости от другого, и наоборот");
        add(CreateDistanceJointBrick.class, "Создает мягкий шарнир для 2-х спрайтов со смещением (как пружина)");
        add(CreateWeldJointBrick.class, "Создает жесткий шарнир для 2-х спрайтов со смещением (как сварка, например скрепили 2 обьекта палкой)");
        add(CreatePrismaticJointBrick.class, "Создает поршневой шарнир для 2-х спрайтов со смещением (фиксирует по оси)");
        add(DestroyJointBrick.class, "Удаляет соединение (шарнир)");
        add(SetThirdPersonCameraBrick.class, "Устанавливает камеру от 3-го лица. Я сам хз как, удачи разобраться");
        add(SetFreeCameraBrick.class, "Возвращает камеру к свободному состоянию");
        add(ApplyForceBrick.class, "Применяет силу к спрайту");
        add(ApplyImpulseBrick.class, "Применяет импульс к спрайту");
        add(ApplyTorqueBrick.class, "Прилагает вращение к спрайту");
        add(ApplyAngularImpulseBrick.class, "Придает крутящий импульс спрайту");
        add(SetDampingBrick.class, "Задает затухание, имитируя воду или сопротивление воздуха");
        add(PerformRayCastBrick.class, "Пускает луч, может пригодиться для врагов, или стрельбы. ну а так же и др.");
        add(VibrationBrick.class, "Заставляет устройство вибрировать");
        add(SetPhysicsObjectTypeBrick.class, "Задает тип движения обьекта: динамичный - падает и подчиняется законам ньютона. статический - не падает, но от него отталкиваются динамические");
        add(SetHitboxBrick.class, "Оставляет хитбокс текущего образа, но изменяет видимый образ. ВАЖНО: плохо работает");
        add(SetVelocityBrick.class, "Задает скорость динамическому телу");
        add(TurnRightSpeedBrick.class, "Задает скорость вращения");
        add(TurnLeftSpeedBrick.class, "Задает скорость вращения");
        add(SetGravityBrick.class, "Задает гравитацию всему физическому миру");
        add(SetMassBrick.class, "Задает массу динамическому обьекту");
        add(SetRestitutionBrick.class, "Задает упругость обьекта");
        add(SetFrictionBrick.class, "Задает трение обьекта");
        add(IfOnEdgeBounceBrick.class, "Изменяет направление спрайта, если он на краю экрана");
        add(ComeToFrontBrick.class, "Перемещает обьект на передний план");
        add(GoNStepsBackBrick.class, "Изменяет слой обьекта в меньшую сторону");
        add(WhenBounceOffBrick.class, "Вызывается, когда столкновение закончено. ВАЖНО: плохо работает");
        add(PlaySoundBrick.class, "Играет звук");
        add(PlaySoundAndWaitBrick.class, "Играет звук и ждет окончания");
        add(PlaySoundAtBrick.class, "Играет звук с определенного момента");
        add(SoundFileBrick.class, "Играет звук из папки \"Загрузки\" у пользователя");
        add(SoundFilesBrick.class, "Играет звук из файлов проекта");
        add(PrepareSoundBrick.class, "Подготавливает звук к воспроизведению (нужен, чтобы избежать лагов)");
        add(PlayPreparedSoundBrick.class, "Играет подготовленный звук");
        add(StopSoundBrick.class, "Останавливает звук");
        add(StopAllSoundsBrick.class, "Останваливает все звуки на сцене");
        add(SetVolumeToBrick.class, "Задает глобальную громкость всех звуков");
        add(SetSoundVolumeBrick.class, "Задает громкость определенного звука");
        add(ChangeVolumeByNBrick.class, "Изменяет громкость всех звуков");
        add(StartListeningBrick.class, "Начинает прослушивание микрофона в отрезках по выбранному времени и сохраняет громкость в формулу");
        add(StartRecordingBrick.class, "Начинает запись звука микрофона");
        add(StopRecordingBrick.class, "Останавливает запись звука микрофона и сохраняет ее в файлы проекта.");
        add(SpeakBrick.class, "Синтезирует речь и произносит текст");
        add(SpeakAndWaitBrick.class, "Синтезирует речь, произносит текст и ждет окончания");
        add(AskSpeechBrick.class, "Включает распознование речи, чтобы спросить вопрос у пользователя");
        add(SetLookBrick.class, "Задает образ спрайта");
        add(SetLookByIndexBrick.class, "Задает образ спрайта под индексом");
        add(SetWidthBrick.class, "Задает ширину спрайта");
        add(SetHeightBrick.class, "Задает высоту спрайта");
        add(ChangeWidthBrick.class, "Изменяет ширину спрайта");
        add(ChangeHeightBrick.class, "Изменяет высоту спрайта");
        add(NextLookBrick.class, "Задает следующий образ");
        add(PreviousLookBrick.class, "Задает предыдущий образ");
        add(SetSizeToBrick.class, "Задает размер спрайта");
        add(EasePropertyBrick.class, "Плавно изменяет свойство обьекта с определенной анимацией");
        add(ChangeSizeByNBrick.class, "Изменяет размер спрайта");
        add(HideBrick.class, "Скрывает спрайт (он становится невидимым)");
        add(ShowBrick.class, "Показывает спрайт (он снова видимый)");
        add(AskBrick.class, "Спрашивает вопрос и сохраняет введенное значение");
        add(BigAskBrick.class, "Расширенный блок \"Спросить\"");
        add(SetTransparencyBrick.class, "Задает прозрачность спрайту");
        add(ChangeTransparencyByNBrick.class, "Изменяет прозрачность спрайта");
        add(SetBrightnessBrick.class, "Задает яркость спрайта");
        add(ChangeBrightnessByNBrick.class, "Изменяет яркость спрайта");
        add(SetColorBrick.class, "Задает цвет спрайта (меняет HUE)");
        add(ChangeColorByNBrick.class, "Изменяет цвет спрайта (меняет HUE)");
        add(FadeParticleEffectBrick.class, "Включает или выключает эффект частиц");
        add(SetParticleColorBrick.class, "Задает цвет частиц");
        add(ClearGraphicEffectBrick.class, "Очищает все графические эффекты (частицы и др.)");
        add(SetCameraFocusPointBrick.class, "Фокусирует камеру на спрайте");
        add(SetCameraPosition2Brick.class, "Задает позицию камере");
        add(SetCameraRotation2Brick.class, "задает вращение камеры");
        add(SetCameraZoomBrick.class, "Задает приближение камере");
        add(PinToCameraBrick.class, "Прикрепляет спрайт к камере (при вращении и перемещении он остается там же, где и был). Полезно для интерфейсов игр");
        add(UnpinFromCameraBrick.class, "Открепляет спрайт от камеры (не возвращает его на начальную позицию! С этим вы сами справляйтесь ;)  )");
        add(SetBackgroundBrick.class, "Задает образ фону");
        add(SetBackgroundByIndexBrick.class, "Задает образ фону по номером");
        add(SetBackgroundAndWaitBrick.class, "Задает образ фону и ждет окончания всех событий, слушающих это");
        add(SetBackgroundByIndexAndWaitBrick.class, "Задает образ фону по номеру и ждет окончания всех событий, слушающих это");
        add(CameraBrick.class, "Включает или выключает камеру устройства (и отображает ее на заднем плане)");
        add(ChooseCameraBrick.class, "Определяет, какую камеру использовать: переднюю или заднюю");
        add(FlashBrick.class, "Включает или выключает фонарик");
        add(LookRequestBrick.class, "Получает изображение по ссылке и пытается использовать как образ");
        add(ScreenShotBrick.class, "Делает скриншот сцены и использует его как образ");
        add(PhotoBrick.class, "Делает снимок с камеры и использует его как образ");
        add(SaveLookBrick.class, "Сохраняет образ в \"Загрузки\" пользователя");
        add(SaveLookFilesBrick.class, "Сохраняет образ в файлы проекта");
        add(LookFileBrick.class, "Использует образ из файла в папке \"Загрузки\" пользователя");
        add(SetLookFilesBrick.class, "Использует образ из файла проекта");
        add(LookToTableBrick.class, "Сохраняет образ по пикселям в таблицы");
        add(LookFromTableBrick.class, "Загружает образ из таблиц по пикселям");
        add(PaintNewLookBrick.class, "Открывает Pocket Paint, чтобы пользователь нарисовал образ");
        add(EditLookBrick.class, "Открывает Pocket Paint, чтобы пользователь изменил текущий образ");
        add(CopyLookBrick.class, "Копирует образ");
        add(DeleteLookBrick.class, "Удаляет образ ВАЖНО: навсегда");
        add(SquareBrick.class, "Создает квадрат");
        add(DelSquareBrick.class, "Удаляет квадрат");
        add(CreateDialogBrick.class, "Создает диалог. Вы можете сами настроить поля ввода, а также кнопки");
        add(SetPositiveBrick.class, "Добавляет в диалог позитивную кнопку (возвращает: 1)");
        add(SetNeutralBrick.class, "Добавляет в диалог нейтральную кнопку (возвращает: 0)");
        add(SetNegativeBrick.class, "Добавляет в диалог негативную кнопку (возвращает: -1)");
        add(AddEditBrick.class, "Добавляет поле ввода в диалог (возвращает: написанное значение)");
        add(AddRadioBrick.class, "Добавляет выбор параметра в диалог (возвращает: только 1 выбранный параметр)");
        add(SetCallbackBrick.class, "Задает переменную, в которую вернется значение диалога");
        add(ShowDialogBrick.class, "Показывает готовый диалог ВАЖНО: не ждет окончания, продолжает выполнять код дальше");
        add(WriteVariableOnDeviceBrick.class, "Записывает переменную в специальный файл в проекте");
        add(ReadVariableFromDeviceBrick.class, "Читает переменную из специального файла в проекте");
        add(WriteVariableToFileBrick.class, "Записывает переменную в файл в \"Загрузки\" пользователя");
        add(ReadVariableFromFileBrick.class, "Читает переменную из файла в \"Загрузках\" пользователя");
        add(WriteToFilesBrick.class, "Сохраняет переменную в файлы проекта");
        add(ReadFromFilesBrick.class, "Читает переменную из файлов проекта");
        add(ZipProjectFilesBrick.class, "Запаковать файлы в файлы проекта в ZIP");
        add(UnzipProjectFilesBrick.class, "Распаковать ZIP архив в файлы проекта");
        add(HttpCreateBrick.class, "HTTP: Создать пустую заготовку GET/POST/PUT/DELETE запроса с уникальным ID.");
        add(HttpConfigBrick.class, "HTTP: Добавить в запрос заголовки, параметры URL, настроить таймауты, куки или прокси.");
        add(HttpBodyBrick.class, "HTTP: Указать текстовое тело запроса (JSON/XML) и формат Content-Type.");
        add(HttpAttachFileBrick.class, "HTTP: Прикрепить локальный файл из проекта к запросу для последующей отправки.");
        add(HttpSendBrick.class, "HTTP: Выполнить настроенный запрос и подождать ответа от сервера.");
        add(HttpSaveFileBrick.class, "HTTP: Записать полученный ответ (например, скачанную картинку) в файлы проекта.");
        add(DeleteFilesBrick.class, "Удаляет файл проекта ВАЖНО: может вызвать фатальную ошибку, если файла нет.");
        add(FileUrlBrick.class, "Сохраняет файл по ссылке в \"Загрузки\" пользователя");
        add(FilesUrlBrick.class, "Сохраняет файл по ссылке в файлы проекта");
        add(ZipBrick.class, "Запаковывает файлы из \"Загрузок\" пользователя в .zip архив. ВАЖНО: файлы вводятся через запятую. архив так же сохраняетя в \"загрузки\"");
        add(GetZipFileNamesBrick.class, "Возвращает имена всех файлов внутри zip архива ВАЖНО: архив из файлов проекта");
        add(UnzipBrick.class, "Распаковывает .zip архив и сохраняет все файлы в \"Загрузки\" пользователя. ВАЖНО: архив из \"Загрузок\" пользователя");
        add(StoreCSVIntoUserListBrick.class, "Разделяет строку как CSV");
        add(SplitBrick.class, "Разделяет строку через определенный символ (или несколько символов)");
        add(RegexBrick.class, "Расширенное регулярное выражение: сохраняет все результаты в таблицу");
        add(WebRequestBrick.class, "Отправляет GET запрос на сайт и получает результат");
        add(PostWebRequestBrick.class, "Отправляет POST запрос на сайт и получает результат ВАЖНО: не ждет окончания, продолжает выполнять код");
        add(CreateVarBrick.class, "Задает значение переменной ВАЖНО: локальные переменные - тип переменных, который создается по ключу в процессе выполнения. Так же они не очищаются при выходе из проекта (но очищаются при выходе из приложения)");
        add(CreateTableBrick.class, "Создает таблицу с определенным размером ВАЖНО: таблицы - мощный инструмент. Они работают так же, как и локальные переменные");
        add(ShowToastBlock.class, "Показывает сообщение снизу");
        add(CopyTextBrick.class, "Копирует текст в буфер обмена");
        add(RunJSBrick.class, "Выполняет JavaScript и сохраняет результат");
        add(RunLuaBrick.class, "Выполняет ЧИСТЫЙ Lua и сохраняет результат ВАЖНО: Lua в Pocket Up и CCode и Lua в NewCatroid - НЕ ОДНО И ТО ЖЕ! в NewCatroid он не умеет отрисовывать что-то");
        add(LunoScriptBrick.class, "Выполняет LunoScript, это один из самых мощных инструментов, позволяет выполнять почти любой Java код внутри приложения. Документация есть на GitHub: Danveyd/LunoScript");
        add(ClearPythonEnvironmentBrick.class, "Очищает окружение Python");
        add(LoadNativeModuleBrick.class, "Загружает нативный модуль (.so), нужно для некоторых Python библиотек");
        add(LoadPythonLibraryBrick.class, "Загружает библиотеку Python (.whl, .zip), позволяет подключать любые сторонние библиотеки");
        add(RunPythonScriptBrick.class, "Запускает Python 3.12 скрипт ВАЖНО: Python работает только на архитектуре arm64_v8a, на сторонних он не запустится!");
        add(RunShellBrick.class, "Запускает команду в терминале и сохраняет результат ВАЖНО: не ждет окончания, продолжает выполнять код дальше");
        add(OpenFileBrick.class, "Открывает файл из папки \"Загрузки\"");
        add(RunChip8Brick.class, "Запускает ROM эмулируя Chip-8. Позволяет запускать ретро-игры. После запуска создаст переменные. значения по умолчанию (чтоб все работало):\n\nchip8_speed = 10   - скорость процессора\n" +
                "chip8_x = -320   - позиция X окна с игрой\n" +
                "chip8_y = -160   - позиция Y окна с игрой\n" +
                "chip8_width = 640   - ширина окна с игрой\n" +
                "chip8_height = 320   - высота окна с игрой");
        add(SetAIBrick.class, "Дает объекту ИИ для обхода препятствий. Может использоваться например для врага, который должен ходить по карте");
        add(CopyProjectFileBrick.class, "Копирует файл проекта с новым именем");
        add(SetHingeMotorBrick.class, "Задает угол для шарнира, который будет пытаться удержать");
        add(AddHingeBrick.class, "Создает шарнир, вращающийся только по выбранным осям. (например 0, 1, 0 будет работать как дверь. а 0, 0, 1 как люк)");
        add(BakeByPrefixBrick.class, "'Запекает' все статичные обьекты, имеющие в имени префикс, что вы ввели в 1 большой обьект. (Если у вас сотни статичных обьектов используйте это! это поможет в оптимизации)");
        add(AttachRaySensorBrick.class, "Привязывает луч к обьекту (используйте для сенсоров. Это оптимизированнее чем вечно пускать луч)");
        add(RotateCameraByBrick.class, "Поворачивает камеру относительно ее текущего вращения");
        add(AttachToCameraBrick.class, "Привязывает объект к камере (используйте для фонариков / обьектов в руках и др.)");
        add(DetachFromCameraBrick.class, "Отвязывает объект от камеры (UNO Reverse для привязки, хахах)");
        add(PromoteLightBrick.class, "Превращает ваш созданный свет в полноценный обьект (вы можете двигать его, вращать и взаимодействовать как с обычным GameObject из 3D редактора)");
        add(SetMaxPointLightsBrick.class, "Задает сколько максимум может быть динамических точечных светов на сцене. Работают только N ближайших к камере");
        add(CameraTouchControlBrick.class, "Включает вращение камеры пальцем. X, Y, W (ширина), H (высота) ЗАДАЮТСЯ В ПРОЦЕНТАХ ОТ ВСЕГО ЭКРАНА!");
        add(CameraSettingsBrick.class, "Настраивает поле зрения и тряску камеры");
        add(MoveFilesBrick.class, "Копирует файл проекта в \"Загрузки\"");
        add(MoveDownloadsBrick.class, "Копирует файл из \"Загрузок\" в файлы проекта");
        add(OrientationBrick.class, "Меняет ориентацию проекта ВАЖНО: блок тут вообще по приколу и работает плохо");
        add(CreateWebFileBrick.class, "Создает WebView из HTML кода");
        add(CreateWebUrlBrick.class, "Создает WebView по ссылке");
        add(SetWebBrick.class, "Задает callback к WebView, чтобы из WebView можно было отправить значение в переменную NewCatroid, через Android.postMessage(\"Значение\");");
        add(EvalWebBrick.class, "Выполняет JavaScript в контексте WebView");
        add(CreateVideoBrick.class, "Создает VideoPlayer, который позволяет просматривать видео из файлов проекта");
        add(CreateTextFieldBrick.class, "Создает TextField (текстовое поле), которое позволяет вводить текст");
        add(AttachSOBrick.class, "Подключает .so файл к GL сцене. ВАЖНО: файл должен соответствовать API и быть той же архитектуры, что и устройство.");
        add(CreateGLViewBrick.class, "Создает GL сцену, позволяя создавать высокооптимизированные сцены");
        add(DeleteWebBrick.class, "Удаляет любой View: WebView, VideoPlayer, TextField, GL сцену и др.");
        add(RunVMBrick.class, "Запускает виртуальную машину с помощью QEMU на архитектуре x86_64 (позволяет запускать Windows, Linux и др) ВАЖНО: обязательно выключайте VM, когда она не нужна");
        add(RunVm2Brick.class, "Запускает виртуальную машину с помощью QEMU на архитектуре x86_64, при помощи аргументов (позволяет запускать Windows, Linux и др) ВАЖНО: обязательно выключайте VM, когда она не нужна");
        add(CreateDiskBrick.class, "Создает виртуальный жесткий диск для VM");
        add(VmSetMonitorSizeBrick.class, "Задает размер монитора VM (VNC)");
        add(ToggleDisplayBrick.class, "Определяет: показывать ли изображение VM");
        add(MouseEventBrick.class, "Эмулирует событие мыши для VM (VNC). ВАЖНО: Маска - побитовая информация о кнопках: " +
                "1: Левая кнопка мыши (Button 1 Pressed/Released).\n" +
                "2: Средняя кнопка мыши (Button 2 Pressed/Released).\n" +
                "4: Правая кнопка мыши (Button 3 Pressed/Released).\n" +
                "8: Кнопка 4 (прокрутка вверх, если поддерживается).\n" +
                "16: Кнопка 5 (прокрутка вниз, если поддерживается).\n" +
                "Например, нажатие ЛКМ будет 1, а нажатие одновременно ЛКМ и ПКМ будет: 1 + 4 = 5 (т.к. ЛКМ: 1, а ПКМ: 4)");
        add(VmRelativeMouseMoveBrick.class, "Эмулирует событие мыши для VM (VNC), НО СМЕЩАЕТ МЫШЬ ОТНОСИТЕЛЬНО ТЕКУЩЕЙ ПОЗИЦИИ!. ВАЖНО: Маска - побитовая информация о кнопках: " +
                "1: Левая кнопка мыши (Button 1 Pressed/Released).\n" +
                "2: Средняя кнопка мыши (Button 2 Pressed/Released).\n" +
                "4: Правая кнопка мыши (Button 3 Pressed/Released).\n" +
                "8: Кнопка 4 (прокрутка вверх, если поддерживается).\n" +
                "16: Кнопка 5 (прокрутка вниз, если поддерживается).\n" +
                "Например, нажатие ЛКМ будет 1, а нажатие одновременно ЛКМ и ПКМ будет: 1 + 4 = 5 (т.к. ЛКМ: 1, а ПКМ: 4)");
        add(KeyEventBrick.class, "Эмулирует событие клавиш для VM (VNC). ВАЖНО: специальные клавиши обозначаются так: <CTRL_L>, <CTRL_R>, <ALT_L>, <ALT_R>, <WIN_L>, <WIN_R>, <SHIFT_L>, <SHIFT_R>, <DEL>, <BACKSPACE>, <ENTER>, <ESC>, <TAB>, <UP>, <DOWN>, <LEFT>, <RIGHT>, <UP>, <DOWN>");
        add(StopVMBrick.class, "Останавливает VM");
        add(SendVmInputBrick.class, "Отправляет клавиши как физическая клавиатура (может пригодиться для систем без дисплея) ВАЖНО: спец. символы: <CTRL+ANY> (ANY - любой символ, тобеж: <CTRL+C>, <CTRL+O>), <ESC>, <TAB>, <ENTER>, <BACKSPACE>, <UP>, <DOWN>, <RIGHT>, <LEFT>");
        add(BindVmOutputBrick.class, "Привязывает вывод консоли VM к переменной. ВАЖНО: сначала привяжите, и только потом запускайте VM, иначе она запустится без этого. Честно, я пока-что сам хз как этим пользоваться, но мне сказали, что это надо :/");
        add(ChooseFileBrick.class, "Открывает меню выбора файла и сохраняет выбранный файл в файлы проекта.");
        add(SaveToInternalStorageBrick.class, "Сохраняет файл проекта по пути внутрь хранилища приложения (data/data/...)");
        add(LoadFromInternalStorageBrick.class, "Копирует файл из хранилища приложения (data/data...) в файлы проекта");
        add(ExportProjectFileBrick.class, "Открывает меню сохранения файла и сохраняет выбранный файл проекта туда, куда выбрал пользователь");
        add(Create3dObjectBrick.class, "Создает 3D обьект из файла. ВАЖНО: поддерживаемые форматы: obj, .glb, .gltf и некоторые другие");
        add(CreateCubeBrick.class, "Создает 3D куб ВАЖНО: не работает в рендере версии 2.0 (куб невидимый)");
        add(CreateSphereBrick.class, "Создает 3D сферу ВАЖНО: не работает в рендере версии 2.0 (сфера невидимая)");
        add(Remove3dObjectBrick.class, "Удаляет 3D обьект");
        add(SetObjectColorBrick.class, "Задает цвет обьекту ВАЖНО: не работает в рендере версии 2.0");
        add(SetObjectTextureBrick.class, "Задает текстуру обьекту ВАЖНО: не работает в рендере версии 2.0\nЕсли в начале написать \"buffer://\" а потом имя буфера - текстура возьмется от туда (нужно чтоб отображать 2D сцены в 3D)");
        add(Set3dScaleBrick.class, "Задает размер обьекта ВАЖНО: во избежании проблем используйте одинаковые размеры по всем осям, например: 1, 1, 1; 0.1, 0.1, 0.1; 3, 3, 3 и т.д.");
        add(SetPhysicsStateBrick.class, "Задает физическое состояние обьекта: динамичный, статичный (примитив - куб), сложный статичный (повторяет форму модели)");
        add(SetCCDBrick.class, "Включает или выключает CCD (точное столкновение) для обьекта (включайте для динамичных обьектов)");
        add(SetShaderCodeBrick.class, "Задает код 3D шейдера. (GLSL)  ВАЖНО: для старого рендера");
        add(SetScreenShaderBrick.class, "Задает код 3D шейдера. (GLSL)  ВАЖНО: для нового рендера (2.0)");
        add(SetObjectShaderBrick.class, "Задает шейдер для определенного обьекта. Используйте для волн, травинок и др. ВАЖНО: для рендера 2.0");
        add(SetObjectShaderUniformBrick.class, "Задает uniform'у (параметр) для шейдера объекта");
        add(CameraTrackingBrick.class, "Позволяет привязать камеру к объекту по позиции или вращению (ничего не будет дергаться! :>  )");
        add(CreateParticlesBrick.class, "Создает частицы ВАЖНО: так же создает GameObject с данным ID, указывайте уникальный ID");
        add(DeleteParticlesBrick.class, "Удаляет частицы");
        add(RemoveObjectsByPrefixBrick.class, "Удаляет все объекты с определенным префиксом");
        add(SetShadowQualityBrick.class, "Изменяет параметры теней в сцене. ВАЖНО: В РАЗРЕШЕНИИ ЛУЧШЕ УКАЗЫВАТЬ СТЕПЕНЬ 2-ки! (128, 512, 2048, 4096...)");
        add(SetPostProcessingNewBrick.class, "Может включить и настроить эффекты для постобработки. (для рендера 2.0)");
        add(SetPostProcessingBrick.class, "Может включить эффект для постобработки, настроить его параметры (ДАННЫЙ БЛОК УСТАРЕЛ. СОВЕТУЕТСЯ ИСПОЛЬЗОВАТЬ ДРУГОЙ АНАЛОГИЧНЫЙ БЛОК)");
        add(PrepareSoundBrick2.class, "Подготавливает звук к воспроизведению ВАЖНО: КАК АУДИО! рекомендуется для sfx и коротких звуков");
        add(PrepareMusicAs3DSoundBrick.class, "Подготавливает музыку к воспроизведению ВАЖНО: КАК МУЗЫКУ! рекомендуется для воспроизведения длинной музыки");
        add(PlaySoundAtPositionBrick.class, "Воспроизводит подготовленный звук в 3D пространстве");
        add(StopSoundBrick2.class, "Останавливает 3D звук");
        add(Set3DSoundMaxDistanceBrick.class, "Задает максимальную дистанцию, на которой будет слышно звук");
        add(LoadSceneAdditiveBrick.class, "Загружает сцену НЕ ЗАМЕНЯЯ ТЕКУЩУЮ! ВАЖНО: дублирующиеся имена будут с постфиксом (1), например был обьект 'player', при добавлении новой сцены с еще одним 'player' он будет переименован в 'player (1)'");
        add(Set3DSoundPositionBrick.class, "Задает позицию 3D звуку");
        add(SetGlobalSoundVolumeBrick.class, "Задает глобальную громкость 3D звуков");
        add(SetMaterialBrick.class, "Задает PBR материал. ВАЖНО: только для рендера 2.0.\nЕсли в начале написать \"buffer://\" а потом имя буфера - текстура возьмется от туда (нужно чтоб отображать 2D сцены в 3D)");
        add(CloneObjectBrick.class, "Клонирует 3D обьект");
        add(SetRenderResolutionBrick.class, "Задает разрешение и соотношение сторон для 3D рендера (включает постобработку + работает только в 2.0)");
        add(SetFpsBrick.class, "Задает лимит FPS (кадров в секунду) для 3D рендера");
        add(ApplyShaderToImageBrick.class, "Применяет шейдер к файлу проекта (изображению). Используйте это для генерации чего-либо или обработки изображений");
        add(SetActiveBrick.class, "Включает или выключает обьект (как в Unity). ВАЖНО: дети обьекта тоже выключаются вместе с ним");
        add(SetParentBrick.class, "Задает обьекту родителя (полезно для рук, шапок, камеры от 1 лица, ящиков и др.). P.S. обьект двигается и вращается вместо со своим родителем");
        add(RemoveParentBrick.class, "Удаляет родителя для обьекта");
        add(CreatePointJointBrick.class, "Создает сферический шарнир для двух 3D обьектов");
        add(BufferMaskBrick.class, "Накладывает выбранный буфер как маску на текущий спрайт. Режимы: Растянуть (маска размера обьекта) / По экрану (глобальная маска). Можно передать 'none' для снятия маски.");
        add(RemoveJointBrick.class, "Удаляет шарнир для 3D обьектов");
        add(EnablePbrRenderBrick.class, "Включает новый рендер 2.0, поддерживающий PBR");
        add(PlayAnimationBrick.class, "Воспроизводит анимацию для обьекта ВАЖНО: только для GLB и GLTF моделей");
        add(SetAnisotropicFilterBrick.class, "Задает уровень анизотропной фильтрации");
        add(SetPointLightBrick.class, "Создает точечный свет (лампочка)");
        add(SetSpotLightBrick.class, "Создает направленный свет (прожектор)");
        add(SetDirectionalLightBrick.class, "Задает глобальный свет (отбрасывает тени)");
        add(SetNativeParentBrick.class, "Перемещает нативное окно внутрь указанного контейнера прокрутки (или возвращает на сцену, если поле родителя оставить пустым)");
        add(CreateScrollViewBrick.class, "Создает нативную область прокрутки (ScrollView) с настройками отступов, отображения полос и эффектов краев");
        add(CreateTextLabelBrick.class, "Создает текстовое окно с поддержкой HTML форматирования, скругления фона, кастомного шрифта и встроенной прокрутки");
        add(NativeViewConfigBrick.class, "Редактирует параметры нативного View. (очень... много... параметров...)");
        add(LoadSceneBrick.class, "Загружает сцену из .rscene файла, созданную в редакторе. ВАЖНО: НЕОБХОДИМО ВКЛЮЧИТЬ РЕНДЕР 2.0! полностью заменяет текущую.");
        add(StartServerBrick.class, "Запускает локальный сервер (сервер только в вашей Wi-Fi точке)");
        add(ConnectServerBrick.class, "Подключается к локальному серверу (только в вашей Wi-Fi точке)");
        add(NativeViewAnimateBrick.class, "Анимирует свойство нативного окна (смещение, масштаб, прозрачность, поворот) с помощью выбранного режима сглаживания Easing");
        add(NativeViewListenerBrick.class, "Начать отслеживание событий нативного окна и сохранять результаты в переменную (клики, ввод, загрузка URL, прогресс, скроллинг и т.д.)");
        add(CreateButtonBrick.class, "Создать нативную Material кнопку со скруглением, фоном и кастомным размером");
        add(CreateSwitchBrick.class, "Создать ползунок-переключатель с подписью и состоянием по умолчанию");
        add(CreateImageViewBrick.class, "Создает нативную картинку с поддержкой фоновой загрузки по URL или из проекта, скругления углов и режимов кадрирования");
        add(CreateSliderBrick.class, "Создает нативный ползунок (SeekBar) с поддержкой дробных лимитов");
        add(WhenSpriteReleasedBrick.class, "Вызывается когда палец отпускает спрайт");
        add(WhenFingerMovedOverSpriteBrick.class, "Вызывается все время пока палец перемещается по спрайту");
        add(WhenFingerMovedOnScreenBrick.class, "Вызывается все время пока палец перемещается по экрану");
        add(ShowNotificationBrick.class, "Присылает уведомление :]   ID уведомления - всегда целое число. Вы можете указать ID уже существующего уведомления, чтобы заменить его");
        add(NativeViewBindSpriteBrick.class, "Привязывает нативный View к спрайту: позиция и видимость будут следовать за спрайтом");
        add(NativeViewControlBrick.class, "Отправляет нативному окну команду на выполнение действия (навигация, перемотка, ввод текста, скроллинг и т.д.)");
        add(ListenServerBrick.class, "Постоянно слушает сообщения сервера и сохраняет значения в переменную");
        add(SendServerBrick.class, "Отправляет значение на локальный сервер ВАЖНО: оно так же придет и вам");
        add(StopServerBrick.class, "Останавливает локальный сервер ВАЖНО: только для хоста");
        add(WriteBaseBrick.class, "Записывает значение в FireBase ВАЖНО: если айди базы неправильный - может вызывать фатальную ошибку");
        add(ReadBaseBrick.class, "Читает значение из FireBase ВАЖНО: если айди базы неправильный - может вызывать фатальную ошибку");
        add(DeleteBaseBrick.class, "Удаляет ключ из FireBase ВАЖНО: если айди базы неправильный - может вызывать фатальную ошибку");
        add(UploadFileBrick.class, "Отправляет файл проекта на сервер");
        add(WhenWindowResizedBrick.class, "Вызывается при изменении размеров окна (или перевороте экрана). Используйте для адаптации интерфейса");
        add(WhenAppMinimizedBrick.class, "Вызывается когда приложение свернули с экрана");
        add(WhenAppRestoredBrick.class, "Вызывается когда приложение вернули на экран");
        add(WhenNotificationActionTriggeredBrick.class, "Вызывается при нажатии на кнопку в уведомлении (ID действия принимает!)");
        add(NotificationAddButtonBrick.class, "Добавляет в уведомление кнопку или поле ввода");
        add(EnableBackgroundModeBrick.class, "Запускает фоновую службу для непрерывной работы логики и таймеров игры при заблокированном экране. ВАЖНО: рендер полностью отключается, а логика в этот момент начинает работать по-другому. (INFO: ID уведомления 1998)");
        add(NotificationCancelBrick.class, "Удаляет и скрывает активное уведомление из системной шторки по его ID.");
        add(DisableBackgroundModeBrick.class, "Останавливает фоновую службу работы игры, экономя системные ресурсы телефона.");
        add(NotificationCreateBrick.class, "Создает (или настраивает существующее) сложное уведомление, которое настраивается последующими блоками (ВАЖНО: не показывает его!)");
        add(NotificationShowBrick.class, "Показывает сложное уведомление с задержкой в секундах (укажите 0, чтоб показать прямо сейчас)");
        add(WhenNotificationClickedBrick.class, "Срабатывает когда пользватель нажмет на уведомление с определенным ID");
        add(CreateBufferBrick.class, "Создает виртуальный кадровый буфер (FrameBuffer) с заданным именем и физическим разрешением в пикселях.");
        add(SetBufferModeBrick.class, "Настраивает режим работы буфера: определяет, будет ли в него отрисовываться 2D сцена, 3D сцена или все вместе.");
        add(SetBufferAutoUpdateBrick.class, "Задает режим автоматического обновления буфера. При отключении автообновления буфер будет обновляться только по запросу.");
        add(AddToBufferBrick.class, "Добавляет текущий спрайт, вызвавший этот блок, в очередь рендеринга выбранного буфера.");
        add(RemoveFromBufferBrick.class, "Исключает текущий спрайт из очереди рендеринга выбранного буфера.");
        add(SetBufferOnlyBrick.class, "При значении 1 отключает отрисовку спрайта на физический экран, оставляя его видимым только внутри текстуры буфера.");
        add(SetBufferCameraBrick.class, "Задает параметры 2D камеры буфера: смещение по осям X/Y, степень приближения (Zoom) и угол вращения.");
        add(SetBufferCamera3DBrick.class, "Задает параметры 3D камеры буфера: координаты камеры, углы наклона (Yaw, Pitch, Roll) и поле зрения (FOV).");
        add(ApplyBufferLookBrick.class, "Задает текущему спрайту образ, динамически транслирующий содержимое выбранного буфера в реальном времени.");
        add(SaveBufferBrick.class, "Захватывает текущее изображение буфера и сохраняет его в файлы проекта в формате .png.");
        add(AddTextToBufferBrick.class, "Добавляет текст для рендеринга внутрь кадрового буфера.");
        add(SetBufferShaderBrick.class, "Устанавливает кастомные вершинный (vsh) и фрагментный (fsh) шейдеры на буфер для постобработки графики.");
        add(SetBufferShaderUniformBrick.class, "Передает кастомные Uniform-переменные (числа или векторы) в шейдер выбранного буфера.");
        add(SetMainRenderLoopsBrick.class, "Позволяет отключать стандартные рендер-циклы (2D, Fast2D, 3D) на дисплей смартфона для экономии ресурсов видеокарты.");
        add(SetBufferEffectsBrick.class, "Позволяет включать 3D VFX эффекты постобработки (Bloom, SSAO, SSR) или мипмаппинг (сглаживание под углами) внутри буфера.");
        add(ConfigureLightBrick.class, "Динамически настраивает параметры (интенсивность, радиус, цвет, направление) источника света по его ID.");
        add(ConfigureMaterialBrick.class, "Динамически настраивает точечные свойства PBR-материала (цвет, шероховатость, металличность, масштаб текстуры) объекта.");
        add(ConfigureParticlesBrick.class, "Динамически перенастраивает параметры системы частиц (скорость эмиссии, размер, угол конуса, лимит частиц) по ее ID в реальном времени.");
        add(SetTextBufferOnlyBrick.class, "Скрыть текст или переменную с основного экрана, оставив его видимым только внутри указанного буфера.");
        add(StartBufferRecordingBrick.class, "Начать запись указанного графического буфера в MP4 видеофайл с выбором FPS и битрейта.");
        add(StopBufferRecordingBrick.class, "Остановить запись видео из буфера и ожидать завершения сохранения видеофайла в проект.");
        add(JavaCompileBrick.class, "Компилирует выбранные файлы .java проекта (или все .java файлы, если поле пустое) в запускаемый .dex файл. Поддерживает автоимпорт внутренних классов NewCatroid и LibGDX. Ошибки компиляции записывает в выбранную переменную.");
        add(JavaLoadAndRunBrick.class, "Загружает один или несколько .dex/.jar файлов (через запятую для поддержки MultiDEX) и выполняет метод выбранного класса. Передает в Java-код Context, Activity или ViewGroup для отрисовки нативного UI.");
        add(JavaDownloadDepBrick.class, "Скачивает библиотеку Maven/Gradle со всеми её зависимостями или без в папку 'libs' проекта. Возвращает список имен скачанных JAR-файлов через запятую.");
        add(OpenAppBrick.class, "Открыть установленное приложение по его имени пакета (напр. com.google.android.youtube).");
        add(Export3dObjectToGlbBrick.class, "Экспортирует любой 3D объект со сцены вместе со всеми текстурами и материалами в бинарный файл формата .glb и сохраняет его в файлы проекта.");
        add(MqttConnectBrick.class,
                "Мультиплеер: Подключиться к лобби.\n" +
                        "Устанавливает соединение с сервером-брокером для работы мультиплеера. Работает асинхронно - приостанавливает выполнение следующих блоков до успешного подключения.\n" +
                        "• ID лобби: уникальное текстовое имя вашего подключения в проекте (например, \"lobby1\").\n" +
                        "• Хост брокера: адрес сервера. Если оставить пустым, используется бесплатный публичный сервер \"broker.emqx.io\".\n" +
                        "• Порт: сетевой порт брокера (по умолчанию 1883).");

        add(MqttJoinRoomBrick.class,
                "Мультиплеер: Войти в комнату.\n" +
                        "Подписывается на игровой канал (комнату) для обмена данными в реальном времени. Приостанавливает скрипт до успешного входа.\n" +
                        "• ID лобби: имя вашего подключения, заданное при подключении.\n" +
                        "• Имя комнаты: название игрового лобби или комнаты чата (например, \"room_north\").\n" +
                        "• Соль (проект): секретный ключ/имя проекта. Используется для шифрования каналов, чтобы другие пользователи на публичном сервере не могли подключиться к вашей комнате и перехватить данные.");

        add(MqttPublishBrick.class,
                "Мультиплеер: Отправить текст.\n" +
                        "Мгновенно отправляет текстовые данные (координаты, сообщения чата, очки) всем игрокам, находящимся в этой же комнате.\n" +
                        "• ID лобби, Имя комнаты, Соль: должны строго совпадать с параметрами блока «Войти в комнату».\n" +
                        "• Текст сообщения: отправляемые данные. Можно использовать формулы, объединять строки или передавать переменные.");

        add(WhenMqttMessageBrick.class,
                "Мультиплеер: Событие получения сообщения.\n" +
                        "Автоматически запускает прикрепленные под него блоки сценария, как только из указанной комнаты мультиплеера приходит новое сообщение.\n" +
                        "• Имя комнаты: название комнаты, данные из которой мы ожидаем услышать.\n" +
                        "• Примечание: пришедший текст можно извлечь внутри этого события с помощью формулы \"mqtt_message('имя_комнаты')\".");

        add(MqttDisconnectBrick.class,
                "Мультиплеер: Отключить лобби.\n" +
                        "Мгновенно разрывает активное сетевое соединение с брокером и освобождает системные ресурсы устройства.\n" +
                        "• Рекомендуется использовать при выходе из игры в главное меню или завершении уровня, чтобы экономить заряд батареи и трафик пользователя.");
        add(Ease3DPropertyBrick.class, "Линейно или по кривой (Easing) сглаживает переход выбранного 3D-свойства (позиция, вращение или масштаб) объекта в течение заданного времени.");
    }

    private static <T extends Brick> void add(Class<T> brickClass, String description) {
        brickDescriptions.put(brickClass, description);
    }

    public static String getDescription(Brick brick) {
        if (brick == null) {
            return DESC_NOT_FOUND;
        }

        String description = brickDescriptions.get(brick.getClass());

        return (description != null) ? description : DESC_NOT_FOUND;
    }
}
