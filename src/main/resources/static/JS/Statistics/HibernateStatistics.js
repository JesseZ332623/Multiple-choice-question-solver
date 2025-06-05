getHibernateStatisticsData();
executeRepeatedly(2000);

/**
 * 该检测数据指定时间就要刷新一次。 
 * 
 * @param {Number} millisecond 毫秒
*/
function executeRepeatedly(millisecond) {
    setInterval(getHibernateStatisticsData, millisecond);
}

/**
 * 从 meta 标签获取 CSRF Token
 * 
 * @return CSRF Token 对象，示例：
 *         {
 *              csrfHeader: 'X-CSRF-TOKEN', 
 *              csrfToken:  'f3cIUWhjMktKrZ6qNG-X4l8zCJ354S60KH74YP8G5exUCBOeTUE4YlkGCnhnzqnMUEKj0TwFJf-f1xeZERrPU8lkhoplPyOm'
 *         }
*/
function getCSRFToken() 
{
    let csrfToken  = document.querySelector('meta[name="_csrf"]').content;
    let csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    return {csrfHeader, csrfToken};
}

/**
 * 若表格中的数据确实发生了变动，
 * 才进行赋值并展示动画效果。
 * 
 * @param {Element} tdNode      表格中的某个 td 元素
 * @param {Number}  newValue    从后端传来的新的，待更新的值
 */
function addAnimationWithCondition(tdNode, newValue) 
{
    if (!tdNode || newValue === undefined || newValue === null) { return; }

    if (tdNode.textContent != newValue.toString()) 
    {
        tdNode.textContent = newValue;

        tdNode.classList.add('update-animation');
        
        // 动画停留 1 秒后移除
        setTimeout(() => {
            tdNode.classList.remove('update-animation'); }, 1000
        );
    }
}

/**
 * 将数据填充到表格中。
 * 
 * @param {Object}              statisticsData 统计数据
 * @param {NodeListOf<Element>} tdNodes        表格元素们
 */
function setDataToElements(statisticsData, tdNodes)
{
    addAnimationWithCondition(tdNodes[0], statisticsData.queryTimes);
    addAnimationWithCondition(tdNodes[1], statisticsData.entityLoadTimes);
    addAnimationWithCondition(tdNodes[2], statisticsData.setLoadTimes);
    addAnimationWithCondition(tdNodes[3], statisticsData.queryCacheHitTimes);
    addAnimationWithCondition(tdNodes[4], statisticsData.connectObtainTimes);
    addAnimationWithCondition(tdNodes[5], statisticsData.l2CacheHitTimes);

    document.getElementById('last-update').textContent = new Date().toUTCString();
}

/**
 * 获取查询数据统计。
*/
async function getHibernateStatisticsData()
{
    const CSRFToken = getCSRFToken();

    /**
     * statisticsData 示例如下：
     *  {
     *       "queryTimes": 0,
     *       "entityLoadTimes": 0,
     *       "setLoadTimes": 0,
     *       "queryCacheHitTimes": 0,
     *       "connectObtainTimes": 0,
     *       "l2CacheHitTimes": 0
     *   }
     */
    try 
    {
        const statisticsData 
            = await fetch(
                '/api/hibernate/query_statistics',
                {
                    method: 'GET',
                    headers: {
                        'Content-Type' : 'application/json',
                        [CSRFToken.csrfHeader] : CSRFToken.csrfToken
                    }
                }
        );

        if (!statisticsData.ok) {
            throw new Error(`Get statistics data failed. status: ${statisticsData.status}.`);
        }

        const tdNodes 
        = document.querySelectorAll('#hibernate_statistics_data tr > td');

        // console.log(`Total ${tdNodes.length} td tags.`);
        // console.info(statisticsDataJson);

        setDataToElements(await statisticsData.json(), tdNodes);
    }
    catch (error)
    {
        console.log(error.message);
    }
}