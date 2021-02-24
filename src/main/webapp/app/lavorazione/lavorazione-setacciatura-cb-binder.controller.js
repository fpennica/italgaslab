(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LavorazioneSetacciaturaCbBinderController', LavorazioneSetacciaturaCbBinderController);

    LavorazioneSetacciaturaCbBinderController.$inject = ['$scope', '$state', '$stateParams', 'entity', 'Pesata', 'PesataByCampione', 'AlertService', 'uiGridConstants', '$uibModalInstance', '$uibModal', '$interval', '$timeout', '$log'];

    function LavorazioneSetacciaturaCbBinderController ($scope, $state, $stateParams, entity, Pesata, PesataByCampione, AlertService, uiGridConstants, $uibModalInstance, $uibModal, $interval, $timeout, $log, binder) {
        var vm = this;

        vm.campione = entity;

        vm.isDirty = false;
        vm.selectedRowsCount = 0;

        vm.clear = clear;
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        vm.addRow = addRow;
        function addRow() {
            if($scope.gridApi.selection.getSelectedCount() > 0) {
                $scope.gridApi.selection.clearSelectedRows();
            }
            var n = vm.gridOptions.data.length + 1;
            vm.gridOptions.data.push({
                id: null,
                numPesata: n,
                pesoNetto: 0,
                tratt125Mm: 0,
                tratt63Mm: 0,
                tratt31V5Mm: 0,
                tratt16Mm: 0,
                tratt8Mm: 0,
                tratt6V3Mm: 0,
                tratt4Mm: 0,
                tratt2Mm: 0,
                tratt1Mm: 0,
                tratt0V5Mm: 0,
                tratt0V25Mm: 0,
                tratt0V125Mm: 0,
                tratt0V075Mm: 0,
                trattB100Mm: 0,
                trattB6V3Mm: 0,
                trattB2Mm: 0,
                trattB0V5Mm: 0,
                trattCb63Mm :0,
                trattCb40Mm :0,
                trattCb31V5Mm: 0,
                trattCb20Mm: 0,
                trattCb16Mm: 0,
                trattCb14Mm: 0,
                trattCb12V5Mm: 0,
                trattCb10Mm: 0,
                trattCb8Mm: 0,
                trattCb6V3Mm: 0,
                trattCb4Mm: 0,
                trattCb2Mm: 0,
                trattCb1Mm: 0,
                trattCb0V5Mm: 0,
                trattCb0V25Mm:0,
                trattCb0V063Mm: 0,
                fondo: 0
            });
         }

        var headerTmpl = '<div ng-class="{ \'sortable\': sortable }"><div class="ui-grid-vertical-bar">&nbsp;</div><div class="ui-grid-cell-contents" col-index="renderIndex"><span>&Phi;<br/>{{ col.displayName }}</span><span ui-grid-visible="col.sort.direction" ng-class="{ \'ui-grid-icon-up-dir\': col.sort.direction == asc, \'ui-grid-icon-down-dir\': col.sort.direction == desc, \'ui-grid-icon-blank\': !col.sort.direction }">&nbsp;</span></div><div class="ui-grid-column-menu-button" ng-if="grid.options.enableColumnMenus && !col.isRowHeader  && col.colDef.enableColumnMenu !== false" class="ui-grid-column-menu-button" ng-click="toggleMenu($event)"><i class="ui-grid-icon-angle-down">&nbsp;</i></div><div ng-if="filterable" class="ui-grid-filter-container" ng-repeat="colFilter in col.filters"><input type="text" class="ui-grid-filter-input" ng-model="colFilter.term" ng-click="$event.stopPropagation()" ng-attr-placeholder="{{colFilter.placeholder || \'\'}}" /><div class="ui-grid-filter-button" ng-click="colFilter.term = null"><i class="ui-grid-icon-cancel" ng-show="!!colFilter.term">&nbsp;</i> <!-- use !! because angular interprets \'f\' as false --></div></div></div>';
        var footerCellTmpl = '<div class="ui-grid-cell-contents" >{{col.getAggregationValue() | number:2}}</div>';

        var columnDefs = [
            { field: 'numPesata', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', pinnedLeft:true, enablePinning:false, enableHiding: false,  displayName: 'Pesata n°', sort: { direction: uiGridConstants.ASC, priority: 1 } },
            { field: 'pesoNetto', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: 'Peso Netto [g]', aggregationHideLabel: true, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb63Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '63mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb40Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '40mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb31V5Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '31,5mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb20Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '20mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb16Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '16mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb14Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '14mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb12V5Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '12,5mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb10Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '10mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb8Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '8mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb6V3Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '6,3mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb4Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '4mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb2Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '2mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb1Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '1mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb0V5Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '0,5mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb0V25Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '0,25mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'trattCb0V063Mm', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: '0,063mm [g]', aggregationHideLabel: true, headerCellTemplate: headerTmpl, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl },
            { field: 'fondo', validators: {required: true}, cellTemplate: 'ui-grid/cellTitleValidator', width: '5,2%', type: 'number', aggregationType: uiGridConstants.aggregationTypes.sum, displayName: 'Fondo [g]', aggregationHideLabel: true, enableSorting: false, cellFilter: 'number:2', footerCellTemplate: footerCellTmpl }
        ];

        vm.gridOptions = {
            showGridFooter: true,
            showColumnFooter: true,
            enableCellEditOnFocus: true,
            columnDefs: columnDefs,
            rowEditWaitInterval: -1,
            onRegisterApi: function(gridApi) {
                $scope.gridApi = gridApi;
                // call resize every 500 ms for 5 s after modal finishes opening - usually only necessary on a bootstrap modal
                $interval(function() {
                    //non va su chrome linux
                    //$scope.gridApi.core.handleWindowResize();
                    $scope.gridApi.core.queueGridRefresh();
                }, 1000, 5);
                gridApi.rowEdit.on.saveRow($scope, saveRow);
                gridApi.edit.on.afterCellEdit($scope, function(rowEntity, colDef, newValue, oldValue) {
                    //console.log("afterCellEdit");
                    vm.isDirty = true;
                    updateVerificaSetacciatura();
                });
                gridApi.selection.on.rowSelectionChanged($scope, function(row) {
                    vm.selectedRowsCount = $scope.gridApi.selection.getSelectedCount();
                 });
                 gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows) {
                     vm.selectedRowsCount = $scope.gridApi.selection.getSelectedCount();
                  });
                // gridApi.validate.on.validationFailed($scope,function(rowEntity, colDef, newValue, oldValue) {
                //     vm.invalid = true;
                // });
                // gridApi.core.on.rowsRendered($scope, function () {
                //     $interval(function () {
                //         var rowsRendred = $scope.gridApi.grid.renderContainers.body.renderedRows;
                //         rowsRendred.forEach(function (row) {
                //             row.grid.options.columnDefs.forEach(function (colDef) {
                //                 $scope.gridApi.grid.validate.runValidators(row.entity, colDef, row.entity[colDef.field], NaN, $scope.gridApi.grid);
                //             });
                //         });
                //     }, 500, 1);
                // });
                // gridApi.core.on.rowsRendered($scope, function(){
                //     updateVerificaSetacciatura();
                // });
            }
        };

        function saveRow (rowentity) {
            rowentity.campione = vm.campione;
            //vm.isSaving = true;
            if (rowentity.id !== null) {
                $scope.gridApi.rowEdit.setSavePromise( rowentity, Pesata.update(rowentity, onSaveSuccess, onSaveError).$promise );
                //Pesata.update(rowentity, onSaveSuccess, onSaveError);
            } else {
                $scope.gridApi.rowEdit.setSavePromise( rowentity, Pesata.save(rowentity, onSaveSuccess, onSaveError).$promise );
                //Pesata.save(rowentity, onSaveSuccess, onSaveError);
            }
        }
        function onSaveSuccess (result) {
            $scope.$emit('italgaslabApp:pesataUpdate', result);
            //$uibModalInstance.close(result);
            //vm.isSaving = false;
            //console.log('dirty rows: ' + $scope.gridDirtyRows.length);
        }
        function onSaveError () {
            //vm.isSaving = false;
        }

        vm.onSaveGrid = onSaveGrid;
        function onSaveGrid(binder) {
            //$scope.gridDirtyRows = $scope.gridApi.rowEdit.getDirtyRows($scope.gridApi.grid);
            //console.log('dirty rows: ' + $scope.gridDirtyRows.length);
            //vm.invalid = false;

            var rows = $scope.gridApi.grid.renderContainers.body.renderedRows;
            //var rows = vm.gridOptions.data;
            var exit = false;

            angular.forEach(rows, function (row) {
                //console.log(row.entity);
                row.entity.binder = binder;
                for(var key in row.entity) {
                    //console.log(key + " - " + row.entity[key]);
                    if(key != "id" && row.entity[key] === null) {
                        //console.log(key + " - " + row.entity[key]);
                        row.entity[key] = 0;
                        //exit = true;
                        //break;
                    }
                }
            });
            if(exit) {
                //console.log('esco');
                return;
            }

            if($scope.gridApi.selection.getSelectedCount() > 0) {
                $scope.gridApi.selection.clearSelectedRows();
            }
            $scope.gridApi.rowEdit.flushDirtyRows($scope.gridApi.grid).then(function() {
                //$state.go('pesata', null, { reload: true });
                //console.log("saved all");
                //$state.reload();
                loadAll();
                vm.isDirty = false;
            }, function() {
                //$state.go('pesata');
                //console.log("not saved all");
            });

            // $scope.gridDirtyRows.forEach(
            //     console.log(dirty.entity)
            // );}
        }

        vm.confirmDeleteRows = confirmDeleteRows;
        function confirmDeleteRows() {
            if($scope.gridApi.selection.getSelectedCount() === 0)
                return;
            var modalInstance = $uibModal.open({
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                backdrop: 'static',
                templateUrl: 'app/components/dialogs/confirm-dialog-danger.html',
                controller: ['$uibModalInstance', function($uibModalInstance) {
                    var vm = this;
                    vm.msg = "Sicuro di voler eliminare le righe selezionate?";
                    vm.ok = function() {
                        $uibModalInstance.close();
                    };
                    vm.cancel = function() {
                        $uibModalInstance.dismiss();
                    };
                }],
                controllerAs: 'vm'
            });

            modalInstance.result.then(function(msg) {
                $log.info(' Modal closed at: ' + new Date());
                onDeleteRows();
            }, function(msg) {
                $log.info(' Modal dismissed at: ' + new Date());
            });
        }

        vm.onDeleteRows = onDeleteRows;
        function onDeleteRows() {
            var selectedRows = $scope.gridApi.selection.getSelectedRows();
            var count = $scope.gridApi.selection.getSelectedCount();
            angular.forEach(selectedRows, function(entity, key) {
                if(entity.id !== null) {
                    //console.log('count: ' + count);
                    Pesata.delete({id: entity.id},
                        function () {
                            //console.log('row ' + entity.id +  ' deleted');
                            count -= 1;
                            if(count === 0) {
                                //console.log('count: ' + count + ' ; ricarico tutto');
                                loadAll();
                            }
                        });
                } else {
                    var index = vm.pesatas.indexOf(entity);
                    vm.pesatas.splice(index, 1);
                    count -= 1;
                    //console.log('count: ' + count);
                }
                $scope.gridApi.selection.unSelectRow(entity);
            });
            //loadAll();
            //var gridDirtyRows = $scope.gridApi.rowEdit.getDirtyRows($scope.gridApi.grid);
        }

        vm.verificaSetacciaturaSum = 0;
        vm.verificaSetacciaturaPesoNetto = 0;
        vm.updateVerificaSetacciatura = updateVerificaSetacciatura;
        function updateVerificaSetacciatura() {
            $timeout(function () {
                var columns = $scope.gridApi.grid.columns.length;
                var sum = 0;
                var pesoNetto = 0;
                for(var i = 2; i < columns; i++) {
                    //console.log($scope.gridApi.grid.columns[i]);
                    if(i == 2) {
                        pesoNetto = $scope.gridApi.grid.columns[i].aggregationValue;
                    } else {
                        sum = sum += $scope.gridApi.grid.columns[i].aggregationValue;
                    }
                }
                vm.verificaSetacciaturaSum = sum;
                vm.verificaSetacciaturaPesoNetto = pesoNetto;
                //console.log(sum);
            },
            1000);
        }

        vm.loadAll = loadAll;

        loadAll();

        function loadAll () {
            PesataByCampione.queryBinder({
                id: $stateParams.id
            }, onSuccess, onError);

            function onSuccess(data, headers) {
                vm.pesatas = data;
                vm.gridOptions.data = data;
                vm.updateVerificaSetacciatura();
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        /*
         * Setacci e coperchi
         */
         if(!vm.campione.cassetta.consegna.laboratorio.coperchi || vm.campione.cassetta.consegna.laboratorio.coperchi.length === 0) {
             vm.campione.cassetta.consegna.laboratorio.coperchi = '[]';
         }
         vm.coperchi = JSON.parse(vm.campione.cassetta.consegna.laboratorio.coperchi);

         vm.subtractCoperchio = function() {
             if(vm.coperchioSel !== null && vm.coperchioSel > 0) {
                 var rows = $scope.gridApi.grid.renderContainers.body.renderedRows;
                 angular.forEach(rows, function (row) {
                     for(var key in row.entity) {
                         //console.log(key + " - " + row.entity[key]);
                         if(key != "id" && key != "pesoNetto" && key != "numPesata" && key != "fondo" && row.entity[key] !== null && row.entity[key] > 0) {
                             row.entity[key] = row.entity[key] - vm.coperchioSel;
                             row.isDirty = true;
                             vm.isDirty = true;
                        }
                     }
                });
                $scope.gridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL);
                updateVerificaSetacciatura();
            }


        };

        vm.subtractSetacci = function() {
            if(!vm.campione.cassetta.consegna.laboratorio.setacci || vm.campione.cassetta.consegna.laboratorio.setacci.length === 0) {
                vm.campione.cassetta.consegna.laboratorio.setacci = '[]';
            }
            vm.setacci = JSON.parse(vm.campione.cassetta.consegna.laboratorio.setacci);

            var rows = $scope.gridApi.grid.renderContainers.body.renderedRows;

            var j = 0;
            angular.forEach(rows, function (row) {
                //console.log(row.entity);
                for(var key in row.entity) {
                    //console.log(key + " - " + row.entity[key]);
                    if(key != "id" && key != "pesoNetto" && row.entity[key] !== null && row.entity[key] > 0) {
                        //console.log(key + " - " + row.entity[key]);
                        switch(key) {
                            case "trattCb63Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "63mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb40Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "40mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb31V5Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "31,5mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb20Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "20mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb16Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "16mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb14Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "14mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb12V5Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "12,5mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb10Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "10mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb8Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "8mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb6V3Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "6,3mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb4Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "4mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb2Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "2mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb1Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "1mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb0V5Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "0,5mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb0V25Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "0,25mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                            case "trattCb0V063Mm":
                                for(j = 0; j < vm.setacci.length; j++) {
                                    if(vm.setacci[j].id === "0,063mm") {
                                        row.entity[key] = row.entity[key] - vm.setacci[j].peso;
                                        row.isDirty = true;
                                        vm.isDirty = true;
                                    }
                                }
                                break;
                        }
                    }
                }

            });
            $scope.gridApi.core.notifyDataChange(uiGridConstants.dataChange.ALL);
            updateVerificaSetacciatura();

        };

    }
})();
