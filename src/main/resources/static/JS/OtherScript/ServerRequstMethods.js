document.addEventListener('DOMContentLoaded', function () {
    const tooltip = document.getElementById('tooltip');
    const methods = document.querySelectorAll('.method');

    methods.forEach(method => {
        method.addEventListener('mouseenter', function (e) {
            const text = this.getAttribute('data-tooltip');
            const rect = this.getBoundingClientRect();
            tooltip.textContent = text;
            tooltip.style.display = 'block';
            // 调整位置为下方显示
            tooltip.style.left = `${rect.left}px`;
            tooltip.style.top = `${rect.bottom + 10}px`;
        });

        method.addEventListener('mouseleave', function () {
            tooltip.style.display = 'none';
        });
    });
});