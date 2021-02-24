(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LaboratorioDialogController', LaboratorioDialogController);

    LaboratorioDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Laboratorio'];

    function LaboratorioDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Laboratorio) {
        var vm = this;

        vm.laboratorio = entity;
        vm.clear = clear;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.laboratorio.id !== null) {
                Laboratorio.update(vm.laboratorio, onSaveSuccess, onSaveError);
            } else {
                Laboratorio.save(vm.laboratorio, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('italgaslabApp:laboratorioUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


        vm.setLogo = function ($file, laboratorio) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        laboratorio.logo = base64Data;
                        laboratorio.logoContentType = $file.type;
                    });
                });
            }
        };

    }
})();
