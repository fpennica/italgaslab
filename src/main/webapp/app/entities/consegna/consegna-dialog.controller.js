(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('ConsegnaDialogController', ConsegnaDialogController);

    ConsegnaDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Consegna', 'Laboratorio', 'Principal'];

    function ConsegnaDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Consegna, Laboratorio, Principal) {
        var vm = this;

        Principal.identity().then(function (user) {
            vm.laboratorio = user.laboratorio;
            entity.laboratorio = vm.laboratorio;
        });

        vm.consegna = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;
        vm.laboratorios = Laboratorio.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.consegna.id !== null) {
                Consegna.update(vm.consegna, onSaveSuccess, onSaveError);
            } else {
                Consegna.save(vm.consegna, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('italgaslabApp:consegnaUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.dataConsegna = false;

        vm.setProtocolloAccettazione = function ($file, consegna) {
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        consegna.protocolloAccettazione = base64Data;
                        consegna.protocolloAccettazioneContentType = $file.type;
                    });
                });
            }
        };

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
