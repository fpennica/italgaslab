(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LavorazioneCampioneDialogController', LavorazioneCampioneDialogController);

    LavorazioneCampioneDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'DataUtils', 'entity', 'Campione', 'OperatoreByLaboratorio', 'Laboratorio'];

    function LavorazioneCampioneDialogController ($timeout, $scope, $stateParams, $uibModalInstance, DataUtils, entity, Campione, OperatoreByLaboratorio, Laboratorio) {
        var vm = this;

        vm.campione = entity;

        if(vm.campione.id) {
            if(!vm.campione.cassetta.consegna.laboratorio.teglie || vm.campione.cassetta.consegna.laboratorio.teglie.length === 0) {
                vm.campione.cassetta.consegna.laboratorio.teglie = "[]";
            }
            vm.teglie = JSON.parse(vm.campione.cassetta.consegna.laboratorio.teglie);
        }

        $stateParams.campioneSelected = vm.campione;

        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.save = save;

        // disabilitato perchè è inutile fare una query su tutte le cassette quando
        // la cassetta corrente viene impostata automaticamente in lavorazione.state.js
        //vm.cassettas = Cassetta.query();

        vm.onPesoTegliaChange = function(teglia) {
            vm.campione.essiccamentoPesoTeglia = teglia;
        };

        if(vm.campione.cassetta.consegna)
            vm.operatores = OperatoreByLaboratorio.query({id : vm.campione.cassetta.consegna.laboratorio.id});

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
            $stateParams.campioneSelected = result;
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

        vm.setFotoEtichetta = function ($file, campione) {
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function(base64Data) {
                    $scope.$apply(function() {
                        campione.fotoEtichetta = base64Data;
                        campione.fotoEtichettaContentType = $file.type;
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
