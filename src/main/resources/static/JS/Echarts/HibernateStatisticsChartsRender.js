// 最多渲染 30 个点
var MAX_POINTS = 30;

// 图表渲染位置
const STATISTICS_CHARTS_ELEMENT
    = document.getElementById('statistics_charts');

// 初始堆叠折线图属性，先写好表总体样式、X 和 Y 轴的数据类别以及空数据。
const INIT_CHART_OPTION
    = {
    large: true,                    // 大数据模式
    progressiveChunkMode: 'mod',    // 分块渲染模式
    backgroundColor: '#0d1117',
    title: {
        text: `Hibernate ORM Statistics`,
        textStyle: {
            color: '#ffffff',
            fontSize: 20
        },
        top: '2%',            // 增加顶部间距
        left: 'center'
    },
    tooltip: {
        backgroundColor: '#161b22',
        borderColor: '#30363d',
        textStyle: { color: '#c9d1d9' }
    },
    legend: {
        textStyle: {
            color: '#8b949e'
        },
        top: '12%',           // 下移图例位置
        itemGap: 15           // 增加图例项间距
    },
    grid: {                   // 新增grid配置控制绘图区域
        top: '20%',
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
        data: []
    },
    yAxis: {
        type: 'value',
        axisLine: {
            lineStyle: {
                color: 'rgb(205, 247, 108)'
            }
        },
        axisLabel: {
            color: '#8b949e',
        },
        splitLine: {
            lineStyle: {
                color: '#21262d'
            }
        }
    },
    series: [{
            name: '总查询次数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
                color: '#87CEEB',
                width: 2
            },
            itemStyle: {
                color: '#edede9',
                borderColor: '#0d1117',
                borderWidth: 2
            },
            data: []
        },
        {
            name: '实体加载次数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
                color: ' rgb(0, 117, 226)',
                width: 2
            },
            itemStyle: {
                color: '#edede9',
                borderColor: '#0d1117',
                borderWidth: 2
            },
            data: []
        },
        {
            name: '集合加载次数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
                color: 'rgb(65, 230, 0)',
                width: 2
            },
            itemStyle: {
                color: '#edede9',
                borderColor: '#0d1117',
                borderWidth: 2
            },
            data: []
        },
        {
            name: '二级缓存命中次数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
                color: 'rgb(221, 244, 245)',
                width: 2
            },
            itemStyle: {
                color: '#edede9',
                borderColor: '#0d1117',
                borderWidth: 2
            },
            data: []
        },
        {
            name: '查询缓存命中次数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
                color: 'rgb(207, 54, 54)',
                width: 2
            },
            itemStyle: {
                color: '#edede9',
                borderColor: '#0d1117',
                borderWidth: 2
            },
            data: []
        },
        {
            name: '连接获取次数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 6,
            lineStyle: {
                color: 'rgb(226, 143, 17)',
                width: 2
            },
            itemStyle: {
                color: '#edede9',
                borderColor: '#0d1117',
                borderWidth: 2
            },
            data: []
        }
    ]
};

// 堆叠折线图图表实体
const STATISTICS_CHARTS
    = echarts.init(STATISTICS_CHARTS_ELEMENT);

// 设置初始样式以及适配尺寸
STATISTICS_CHARTS.setOption(INIT_CHART_OPTION);
STATISTICS_CHARTS.resize();

window.addEventListener(
    'resize',
    function() { STATISTICS_CHARTS.resize(); }
);

/**
 * 追加渲染 Hibernate 统计信息堆叠折线图图表。
 * 
 * @param {String}  recordTimeString  记录更新的最新时间
 * @param {Object}  newStatisticsData 由 6 个 Array 组成的统计数据对象。
*/
function appendRenderHibernateStatisticsCharts(recordTimeString, newStatisticsData) 
{
    /**
     * newStatisticsData 示例如下：
     *  {
     *       "queryTimes": 0,
     *       "entityLoadTimes": 0,
     *       "setLoadTimes": 0,
     *       "queryCacheHitTimes": 0,
     *       "connectObtainTimes": 0,
     *       "l2CacheHitTimes": 0
     *   }
     */

    const chartOption = STATISTICS_CHARTS.getOption();

    // 先获取所有所有在表单中的数据
    const updateTimeArray          = chartOption.xAxis[0].data;

    const queryTimesArray          = chartOption.series[0].data;
    const entityLoadTimesArray     = chartOption.series[1].data;
    const setLoadTimesArray        = chartOption.series[2].data;
    const l2CacheHitTimesArray     = chartOption.series[3].data;
    const queryCacheHitTimesArray  = chartOption.series[4].data;
    const connectObtainTimesArray  = chartOption.series[5].data;

    // 由于所有的数组长度都是相等的，所以只需要检测一条即可发现数据是否过多。
    if (updateTimeArray.length >= MAX_POINTS) 
    {
        updateTimeArray.shift();
        queryTimesArray.shift();
        entityLoadTimesArray.shift();
        setLoadTimesArray.shift();
        l2CacheHitTimesArray.shift();
        queryCacheHitTimesArray.shift();
        connectObtainTimesArray.shift();
    }

    updateTimeArray.push(recordTimeString);

    queryTimesArray.push(newStatisticsData.queryTimes);
    entityLoadTimesArray.push(newStatisticsData.entityLoadTimes);
    setLoadTimesArray.push(newStatisticsData.setLoadTimes);
    l2CacheHitTimesArray.push(newStatisticsData.queryCacheHitTimes);
    queryCacheHitTimesArray.push(newStatisticsData.connectObtainTimes);
    connectObtainTimesArray.push(newStatisticsData.l2CacheHitTimes);

    STATISTICS_CHARTS.setOption(
        {
            xAxis: [{data: updateTimeArray}],
            series: [
                {data: queryTimesArray},
                {data: entityLoadTimesArray},
                {data: setLoadTimesArray},
                {data: l2CacheHitTimesArray},
                {data: queryCacheHitTimesArray},
                {data: connectObtainTimesArray}
            ]
        }
    );
}