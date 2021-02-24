(function () {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(pagerConfig);

    pagerConfig.$inject = ['uibPagerConfig', 'paginationConstants'];

    function pagerConfig(uibPagerConfig, paginationConstants) {
        uibPagerConfig.itemsPerPage = paginationConstants.itemsPerPage;
        uibPagerConfig.previousText = '« Pag. precedente';
        uibPagerConfig.nextText = 'Pag. successiva »';
    }
})();
