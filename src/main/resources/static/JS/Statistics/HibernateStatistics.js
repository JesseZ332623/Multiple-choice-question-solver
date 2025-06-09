getHibernateStatisticsData();
executeRepeatedly(1000);

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

        return true;
    }

    return false;
}

/**
 * 将数据填充到表格中。
 * 
 * @param {Object}              statisticsData 统计数据
 * @param {NodeListOf<Element>} tdNodes        表格元素们
 */
function setDataToElements(statisticsData, tdNodes)
{
    const isupdate = new Array();

    isupdate.push(addAnimationWithCondition(tdNodes[0], statisticsData.queryTimes));
    isupdate.push(addAnimationWithCondition(tdNodes[1], statisticsData.entityLoadTimes));
    isupdate.push(addAnimationWithCondition(tdNodes[2], statisticsData.setLoadTimes));
    isupdate.push(addAnimationWithCondition(tdNodes[3], statisticsData.queryCacheHitTimes));
    isupdate.push(addAnimationWithCondition(tdNodes[4], statisticsData.connectObtainTimes));
    isupdate.push(addAnimationWithCondition(tdNodes[5], statisticsData.l2CacheHitTimes));

    const now = new Date().toUTCString();

    document.getElementById('last-update').textContent = now;

    if(isupdate.includes(true)) {
        appendRenderHibernateStatisticsCharts(now, statisticsData);
    }
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

/**
 * 清空统计信息。
 * 
 * @param {Element} button 按钮元素
*/
async function clearStatisticsData(button) 
{
    try 
    {
        const CSRFToken = getCSRFToken();

        const response = await fetch(
            '/api/hibernate/clear_statistics_data', {
                method: 'PUT',
                headers: {
                        [CSRFToken.csrfHeader] : CSRFToken.csrfToken
                    }
            }
        );

        if (!response.ok) {
            throw new Error(`清理统计数据失败 status: ${response.statusText}`);
        }

        button.textContent = '☑️ 完成！';

        console.info(await response.text());

        setTimeout(
            () => button.textContent = '清理统计数据',
            1000
        );
    }
    catch (error)
    {
        console.error(error.message);
        button.textContent = `❌ ${error.message}`;

        setTimeout(
            () => button.textContent = '清理统计数据',
            1000
        );
    }
}