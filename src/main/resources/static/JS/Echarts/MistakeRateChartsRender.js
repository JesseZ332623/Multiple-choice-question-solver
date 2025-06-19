// 总数据条数
var TOTAL_RECORD_AMOUNT
    = Number.parseInt(document.getElementById('record_amount').textContent);

// 一页的数据条数
var ONE_PAGE_AMOUNT
    = Number.parseInt(document.getElementById('one_page_amount').textContent);

// 最后一页的数据条数
var RECORD_AMOUNT_OF_LAST_PAGE
    = TOTAL_RECORD_AMOUNT % ONE_PAGE_AMOUNT;

// 当前的页码
var CURRENT_PAGE
    = Number.parseInt(document.getElementById('current_page').textContent);

// 总页码数
var PAGE_COUNT
    = Number.parseInt(document.getElementById('page_count').textContent);

// 当存在 3 条及以上的记录时，图表渲染才有意义。
if (TOTAL_RECORD_AMOUNT >= 3) {
    renderMistakeRateCharts();
}

/**
  * 渲染指定用户的错误率平滑折线图。
*/
function renderMistakeRateCharts() 
{
    let userName 
        = document.getElementById('user_name_text').textContent;

    let pageAmount = 0;

    // 来到最后一页并且有不足于 ONE_PAGE_AMOUNT 条的数据时。
    if (CURRENT_PAGE === PAGE_COUNT && 
        RECORD_AMOUNT_OF_LAST_PAGE !== 0) {
        pageAmount = RECORD_AMOUNT_OF_LAST_PAGE;
    }
    else {
        // 否则说明数据恰好被页分完
        pageAmount = ONE_PAGE_AMOUNT;
    }

    const mistakeRateMap = new Map();
    
    // 收集数据，X 轴为日期，Y 轴为错误率百分比
    for (let index = 0; index < pageAmount; ++index)
    {
        const submitDate 
                = document.getElementById(`submit_date_${index}`).textContent;

        const mistakeRate 
            = Number.parseFloat(
                document.getElementById(`mistake_rate_${index}`)
                        .textContent.replace('%', '')
            ).toFixed(2);

        mistakeRateMap.set(submitDate, mistakeRate);
    }

    console.log(mistakeRateMap);

    const mistakeRateMapKeyArray   = Array.from(mistakeRateMap.keys());
    const mistakeRateMapValueArray = Array.from(mistakeRateMap.values());

    // 图表渲染位置
    const renderElement    = document.getElementById('mistake_rate_chatrs');

    // 在该位置初始化图表
    const mistakeRateChart = echarts.init(renderElement);

    /**
     * 图标属性，各个属性描述如下：
     * 
     * - backgroundColor 整张表的背景色
     * 
     * - title 标题配置
     *   - text      图表主标题内容
     *   - textStyle 标题文本样式
     * 
     * - tooltip 提示框配置
     *   - backgroundColor 提示框背景色
     *   - borderColor     提示框边框颜色
     *   - textStyle       提示框文本样式
     * 
     * - legend 图例配置
     *   - textStyle 图例文字样式
     *   - top       图例距离顶部的位置
     *   - itemGap   图例项之间的间距（目前只有一个图例，暂时用不到这个属性）
     * 
     * - grid 网格配置
     *   - top              图表绘图区域距离容器顶部的距离
     *   - bottom           图表绘图区域距离容器底部的距离
     *   - containLabel     坐标轴标签是否显示在网格区域内
     * 
     * - xAxis   X 轴配置
     *   - show         是否显示 X 轴（默认为 false）
     *   - type         X 轴数据属性，有 value, category, time, log 四种类型。
     *   - axisLabel    坐标轴标签样式
     *   - data         待展示数据（Array 类型）
     * 
     * - yAxis   Y 轴配置
     *   - 其子属性和 xAxis 类似，不讲了。。。
     * 
     * - series  数据系列配置
     *   - name         系列名称（显示在图例）
     *   - type         我使用的是 line 即折线图类型
     *   - smooth       是否为平滑的
     *   - symbol       数据点标记
     *   - symbolSize   数据点标记大小（单位 px）
     *   - lineStyle    线条样式
     *   - itemStyle    数据点样式
     *   - areaStyle    区域填充渐变
     *   - data         待展示数据（Array 类型）
    */
    const option = {
        lazyUpdate: true,
        animation: true,
        animationDuration: 800,       // 动画时长（毫秒）
        progressiveChunkMode: 'mod',
        animationEasing: 'cubicOut',
        backgroundColor: '#0d1117',
        title: {
            text: `[${mistakeRateMapKeyArray.at(0)}] to [${mistakeRateMapKeyArray.at(mistakeRateMapKeyArray.length - 1)}]`,
            textStyle: {
                color: '#ffffff',
                fontSize: 16
            },
            top:  '2%',            // 增加顶部间距
            left: 'center'
        },
        tooltip: {
            backgroundColor: '#161b22',
            borderColor: '#30363d',
            valueFormatter: (value) => value + '%',
            textStyle: { color: '#c9d1d9' }
        },
        legend: {
            textStyle: {
                color: '#8b949e'
            },
            top: '16%',           // 下移图例位置
            itemGap: 15           // 增加图例项间距
        },
        grid: {                   // 新增grid配置控制绘图区域
            top: '25%',
            bottom: '15%',
            containLabel: true
        },
        xAxis: {
            show: true,
            type: 'category',
            axisLine: {
                lineStyle: {
                    color: '#30363d'
                }
            },
            axisLabel: {
                color: '#8b949e',
                fontSize: 10,     // 缩小字号
                top: '12%'
            },
            data: mistakeRateMapKeyArray
        },
        yAxis: {
            type: 'value',
            min: 0,         // y 轴最小值 0
            max: 100,       // y 轴最大值 100
            interval: 25,   // 间隔为 25
            axisLine: {
                lineStyle: {
                    color: '#30363d'
                }
            },
            axisLabel: {
                color: '#8b949e',
                formatter: (value) => value + '%'     // 在每个值后面加个 %
            },
            splitLine: {
                lineStyle: {
                    color: '#21262d'
                }
            }
        },
        series: [{
            name: 'Mistake Rate',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
                color: '#58a6ff',
                width: 2
            },
            itemStyle: {
                color: '#edede9',
                borderColor: '#0d1117',
                borderWidth: 2
            },
            areaStyle: {
                /**
                 * LinearGradient() 是 Echarts 库提供的渐变对象，构造函数有 4 个参数
                 * LinearGradient(xStart, yStart, xEnd, yEnd, colorStops)
                 * 
                 * (xStart, yStart) 渐变起始点
                 * (xEnd, yEnd)     渐变结束点
                 * colorStops       颜色停止点数组
                */
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                    offset: 0,
                    color: '#1f6feb60'
                }, {
                    offset: 1,
                    color: '#1f6feb00'
                }])
            },
            data: mistakeRateMapValueArray
        }]
    };

    mistakeRateChart.setOption(option);
    mistakeRateChart.resize();

    window.addEventListener(
        'resize', 
        function() { mistakeRateChart.resize(); }
    );
}