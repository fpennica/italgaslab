(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('CampioneDetailController', CampioneDetailController);

    CampioneDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Campione', 'Cassetta', 'Operatore'];

    function CampioneDetailController($scope, $rootScope, $stateParams, DataUtils, entity, Campione, Cassetta, Operatore) {
        var vm = this;

        vm.campione = entity;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('italgaslabApp:campioneUpdate', function(event, result) {
            vm.campione = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
