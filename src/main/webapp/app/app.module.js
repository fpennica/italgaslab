(function() {
    'use strict';

    angular
        .module('italgaslabApp', [
            'ngStorage',
            'tmh.dynamicLocale',
            'pascalprecht.translate',
            'ngResource',
            'ngCookies',
            'ngAria',
            'ngCacheBuster',
            'ngFileUpload',
            'ui.bootstrap',
            'ui.bootstrap.datetimepicker',
            'ui.router',
            'infinite-scroll',
            'ds.objectDiff',
            'ui.mask',
            'ui.grid',
            'ui.grid.resizeColumns',
            'ui.grid.edit',
            'ui.grid.cellNav',
            'ui.grid.pinning',
            'ui.grid.rowEdit',
            'ui.grid.selection',
            'ui.grid.validate',
            'nvd3',
            // jhipster-needle-angularjs-add-module JHipster will add new module here
            'angular-loading-bar'
        ])
        .run(run);

    run.$inject = ['stateHandler', 'translationHandler'];

    function run(stateHandler, translationHandler) {
        stateHandler.initialize();
        translationHandler.initialize();
    }
})();
