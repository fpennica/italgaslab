(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('ConsegnaDetailController', ConsegnaDetailController);

    ConsegnaDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Consegna', 'Laboratorio'];

    function ConsegnaDetailController($scope, $rootScope, $stateParams, DataUtils, entity, Consegna, Laboratorio) {
        var vm = this;

        vm.consegna = entity;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('italgaslabApp:consegnaUpdate', function(event, result) {
            vm.consegna = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
