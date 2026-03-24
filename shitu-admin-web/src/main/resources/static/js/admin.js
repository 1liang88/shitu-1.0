// 通用工具函数
const ShituAdmin = {
    // 格式化日期
    formatDate: function(dateStr, format = 'YYYY-MM-DD HH:mm') {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hour = String(date.getHours()).padStart(2, '0');
        const minute = String(date.getMinutes()).padStart(2, '0');
        const second = String(date.getSeconds()).padStart(2, '0');

        return format
            .replace('YYYY', year)
            .replace('MM', month)
            .replace('DD', day)
            .replace('HH', hour)
            .replace('mm', minute)
            .replace('ss', second);
    },

    // 显示提示消息
    toast: function(message, type = 'success') {
        const toast = $(`
            <div class="position-fixed top-0 end-0 p-3" style="z-index: 9999">
                <div class="toast align-items-center text-white bg-${type} border-0" role="alert">
                    <div class="d-flex">
                        <div class="toast-body">
                            <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'} me-2"></i>
                            ${message}
                        </div>
                        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                    </div>
                </div>
            </div>
        `);

        $('body').append(toast);
        const bsToast = new bootstrap.Toast(toast.find('.toast'));
        bsToast.show();

        setTimeout(() => toast.remove(), 3000);
    },

    // 确认对话框
    confirm: function(message, callback) {
        if (confirm(message)) {
            callback();
        }
    },

    // 加载状态
    loading: {
        show: function(selector = 'body') {
            $(selector).append(`
                <div class="loading-overlay">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">加载中...</span>
                    </div>
                </div>
            `);
        },
        hide: function() {
            $('.loading-overlay').remove();
        }
    }
};

// AJAX全局配置
$.ajaxSetup({
    headers: {
        'X-Requested-With': 'XMLHttpRequest'
    },
    error: function(xhr, status, error) {
        console.error('AJAX Error:', error);
        ShituAdmin.toast('请求失败：' + (xhr.responseJSON?.message || error), 'danger');
    }
});

// 侧边栏折叠（移动端）
$('#sidebarToggle').click(function() {
    $('.sidebar').toggleClass('show');
});

// 点击外部关闭侧边栏
$(document).click(function(e) {
    if ($(window).width() <= 768) {
        if (!$(e.target).closest('.sidebar').length && !$(e.target).closest('#sidebarToggle').length) {
            $('.sidebar').removeClass('show');
        }
    }
});