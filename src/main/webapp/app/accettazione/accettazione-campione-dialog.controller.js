(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('AccettazioneCampioneDialogController', AccettazioneCampioneDialogController);

    AccettazioneCampioneDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Campione'];

    function AccettazioneCampioneDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Campione) {
        var vm = this;

        vm.campione = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
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
            if (vm.campione.id !== null) {
                Campione.update(vm.campione, onSaveSuccess, onSaveError);
            } else {
                Campione.save(vm.campione, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('italgaslabApp:campioneUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


        vm.setFotoSacchetto = function ($file, campione) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        campione.fotoSacchetto = base64Data;
                        campione.fotoSacchettoContentType = $file.type;
                    });
                });
            }
        };

        vm.setFotoCampione = function ($file, campione) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        campione.fotoCampione = base64Data;
                        campione.fotoCampioneContentType = $file.type;
                    });
                });
            }
        };
        vm.datePickerOpenStatus.dataAnalisi = false;

        vm.setCurva = function ($file, campione) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        campione.curva = base64Data;
                        campione.curvaContentType = $file.type;
                    });
                });
            }
        };

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
