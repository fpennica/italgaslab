(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LaboratorioSetacciController', LaboratorioSetacciController);

    LaboratorioSetacciController.$inject = ['$scope', '$rootScope', '$stateParams', 'DataUtils', 'entity', 'Laboratorio', '$timeout'];

    function LaboratorioSetacciController($scope, $rootScope, $stateParams, DataUtils, entity, Laboratorio, $timeout) {
        var vm = this;

        vm.laboratorio = entity;

        if(!vm.laboratorio.setacci || vm.laboratorio.setacci.length === 0) {
            vm.laboratorio.setacci = '[{"id":"125mm","peso":null},{"id":"100mm","peso":null},{"id":"63mm","peso":null},{"id":"40mm","peso":null},{"id":"31,5mm","peso":null},{"id":"20mm","peso":null},{"id":"16mm","peso":null},{"id":"14mm","peso":null},{"id":"12,5mm","peso":null},{"id":"10mm","peso":null},{"id":"8mm","peso":null},{"id":"6,3mm","peso":null},{"id":"4mm","peso":null},{"id":"2mm","peso":null},{"id":"1mm","peso":null},{"id":"0,5mm","peso":null},{"id":"0,25mm","peso":null},{"id":"0,125mm","peso":null},{"id":"0,075mm","peso":null},{"id":"0,063mm","peso":null}]';
        }
        vm.setacci = JSON.parse(vm.laboratorio.setacci);

        if(!vm.laboratorio.coperchi || vm.laboratorio.coperchi.length === 0) {
            vm.laboratorio.coperchi = '[]';
        }
        vm.coperchi = JSON.parse(vm.laboratorio.coperchi);

        vm.save = function() {
            vm.isSaving = true;
            vm.laboratorio.setacci = JSON.stringify(vm.setacci);
            vm.laboratorio.coperchi = JSON.stringify(vm.coperchi);
            if (vm.laboratorio.id !== null) {
                Laboratorio.update(vm.laboratorio, onSaveSuccess, onSaveError);
            } else {
                Laboratorio.save(vm.laboratorio, onSaveSuccess, onSaveError);
            }
        };

        vm.ripristina = function() {
            vm.laboratorio.setacci = '[{"id":"125mm","peso":null},{"id":"100mm","peso":null},{"id":"63mm","peso":null},{"id":"40mm","peso":null},{"id":"31,5mm","peso":null},{"id":"20mm","peso":null},{"id":"16mm","peso":null},{"id":"14mm","peso":null},{"id":"12,5mm","peso":null},{"id":"10mm","peso":null},{"id":"8mm","peso":null},{"id":"6,3mm","peso":null},{"id":"4mm","peso":null},{"id":"2mm","peso":null},{"id":"1mm","peso":null},{"id":"0,5mm","peso":null},{"id":"0,25mm","peso":null},{"id":"0,125mm","peso":null},{"id":"0,075mm","peso":null},{"id":"0,063mm","peso":null}]';
            vm.setacci = JSON.parse(vm.laboratorio.setacci);
        };

        vm.addCoperchio = function() {
            var newItemNo = vm.coperchi.length + 1;
            vm.coperchi.push({
                'id': 'Coperchio' + newItemNo,
                'peso': null
            });
        };

        vm.removeCoperchio = function($index) {
            //var lastItem = vm.teglie.length - 1;
            vm.coperchi.splice($index, 1);
        };

        vm.msg = null;
        function onSaveSuccess(result) {
            $scope.$emit('italgaslabApp:laboratorioUpdate', result);
            //AlertService.success("sdfhsd");
            vm.msg = "Salvataggio effettuato";
            $timeout(function(){
                vm.msg = null;
            }, 3000);
            //$uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }
    }
})();
