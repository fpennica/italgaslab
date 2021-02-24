(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('LavorazioneCampioneController', LavorazioneCampioneController);

    LavorazioneCampioneController.$inject = ['$window', '$scope', '$state', '$stateParams', 'DataUtils', 'Cassetta', 'Campione', 'PesataByCampione', 'CampioneSearch', 'ParseLinks', 'AlertService', 'pagingParams', 'paginationConstants', 'CampioneByCassetta', 'cassetta', '$uibModal'];

    function LavorazioneCampioneController ($window, $scope, $state, $stateParams, DataUtils, Cassetta, Campione, PesataByCampione, CampioneSearch, ParseLinks, AlertService, pagingParams, paginationConstants, CampioneByCassetta, cassetta, $uibModal) {
        var vm = this;

        vm.cassetta = cassetta;
        vm.consegna = cassetta.consegna;

        vm.campioneSelected = $stateParams.campioneSelected;
        vm.campioneSelectedClassGeotec = null;

        vm.numCampioniLavorati = 0;
        vm.updateNumCampioniLavorati = updateNumCampioniLavorati;
        function updateNumCampioniLavorati() {
            var count = 0;
            angular.forEach(vm.campiones, function(campione, key) {
                if(campione.lavorazioneConclusa) {
                    count += 1;
                }
            });
            vm.numCampioniLavorati = count;
        }

        vm.progressBarType = 'success';
        vm.updateProgressBarType = updateProgressBarType;
        function updateProgressBarType() {
            if (vm.numCampioniLavorati / vm.campiones.length < 0.25) {
                vm.progressBarType = 'danger';
            } else if (vm.numCampioniLavorati / vm.campiones.length < 0.75) {
                vm.progressBarType = 'warning';
            } else if (vm.numCampioniLavorati / vm.campiones.length < 1) {
                vm.progressBarType = 'info';
            } else {
                vm.progressBarType = 'success';
            }
        }

        vm.loadPesataByCampione = loadPesataByCampione;
        function loadPesataByCampione() {
            PesataByCampione.query({
                id: vm.campioneSelected.id
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.campioneSelectedPesatas = data;
                if(vm.campioneSelectedPesatas.length > 0) {
                    loadclassificazioneGeotecnica();
                } else {
                    vm.campioneSelectedClassGeotec = null;
                }
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        vm.loadPesataByCampioneCbUsura = loadPesataByCampioneCbUsura;
        function loadPesataByCampioneCbUsura() {
            PesataByCampione.queryUsura({
                id: vm.campioneSelected.id
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.campioneSelectedCbUsuraPesatas = data;
                if(vm.campioneSelectedCbUsuraPesatas.length > 0) {
                    loadCbUsuraPercGran();
                }
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        vm.loadPesataByCampioneCbBinder = loadPesataByCampioneCbBinder;
        function loadPesataByCampioneCbBinder() {
            PesataByCampione.queryBinder({
                id: vm.campioneSelected.id
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.campioneSelectedCbBinderPesatas = data;
                if(vm.campioneSelectedCbBinderPesatas.length > 0) {
                    loadCbBinderPercGran();
                }
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        vm.loadPesataByCampioneWithChart = loadPesataByCampioneWithChart;
        function loadPesataByCampioneWithChart() {
            PesataByCampione.queryWithChart({
                id: vm.campioneSelected.id,
                generateChart: "true"
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.campioneSelectedPesatas = data;
                //per aggiornare lo stato del curvaContentType
                //console.log(vm.campioneSelectedPesatas[0].campione.curvaContentType);
                vm.campioneSelected = vm.campioneSelectedPesatas[0].campione;

                $state.go($state.$current, {
                    campioneSelected: vm.campioneSelected
                }, {reload: true});
                //$state.reload();
                //$state.go('lavorazione.campione', null, { reload: true });
            }
            function onError(error) {
                AlertService.error(error.data.message);
                //$state.reload();
            }
        }

        vm.loadPesataByCampioneWithChartUsura = loadPesataByCampioneWithChartUsura;
        function loadPesataByCampioneWithChartUsura() {
            PesataByCampione.queryWithChartUsura({
                id: vm.campioneSelected.id,
                generateChart: "true"
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.campioneSelectedCbUsuraPesatas = data;
                //per aggiornare lo stato del curvaContentType
                //console.log(vm.campioneSelectedPesatas[0].campione.curvaContentType);
                vm.campioneSelected = vm.campioneSelectedCbUsuraPesatas[0].campione;

                $state.go($state.$current, {
                    campioneSelected: vm.campioneSelected
                }, {reload: true});
                //$state.reload();
                //$state.go('lavorazione.campione', null, { reload: true });
            }
            function onError(error) {
                AlertService.error(error.data.message);
                //$state.reload();
            }
        }

        vm.loadPesataByCampioneWithChartBinder = loadPesataByCampioneWithChartBinder;
        function loadPesataByCampioneWithChartBinder() {
            PesataByCampione.queryWithChartBinder({
                id: vm.campioneSelected.id,
                generateChart: "true"
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.campioneSelectedCbBinderPesatas = data;
                //per aggiornare lo stato del curvaContentType
                //console.log(vm.campioneSelectedPesatas[0].campione.curvaContentType);
                vm.campioneSelected = vm.campioneSelectedCbBinderPesatas[0].campione;

                $state.go($state.$current, {
                    campioneSelected: vm.campioneSelected
                }, {reload: true});
                //$state.reload();
                //$state.go('lavorazione.campione', null, { reload: true });
            }
            function onError(error) {
                AlertService.error(error.data.message);
                //$state.reload();
            }
        }

        vm.loadclassificazioneGeotecnica = loadclassificazioneGeotecnica;
        function loadclassificazioneGeotecnica() {
            Campione.getClassGeotec({
                id: vm.campioneSelected.id
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.campioneSelectedClassGeotec = data;
                updateGeotecData();
                //$state.reload();
            }
            function onError(error) {
                AlertService.error(error.data.message);
                //$state.reload();
            }
        }

        vm.loadCbUsuraPercGran = loadCbUsuraPercGran;
        function loadCbUsuraPercGran() {
            Campione.getCbUsuraPercGran({
                id: vm.campioneSelected.id
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                var ghiaiaPerc = $window.Math.round(data.ghiaiaPerc),
                    sabbiaPerc = $window.Math.round(data.sabbiaPerc),
                    //limoArgillaPerc = 100 - (ghiaiaPerc + sabbiaGrossaPerc + sabbiaPerc);
                    limoArgillaPerc = $window.Math.round(data.limoArgillaPerc);

                var cheat = 0;
                if((ghiaiaPerc + sabbiaPerc + limoArgillaPerc) > 100 && limoArgillaPerc > 0) {
                    cheat = (ghiaiaPerc + sabbiaPerc + limoArgillaPerc) - 100;
                    sabbiaPerc = sabbiaPerc - cheat;
                }

                vm.cbUsuraPercGranData = [
                    {
                        label: "Ghiaia",
                        //value: vm.campioneSelectedClassGeotec.ghiaiaPerc,
                        value: ghiaiaPerc,
                        color: '#2157b4'
                    },
                    {
                        label: "Sabbia",
                        //value: vm.campioneSelectedClassGeotec.sabbiaPerc,
                        value: sabbiaPerc,
                        color: '#b03434'
                    },
                    {
                        label: "Limo e Argilla",
                        //value: vm.campioneSelectedClassGeotec.limoArgillaPerc,
                        value: limoArgillaPerc,
                        color: '#75b44f'
                    }
                ];
            }
            function onError(error) {
                AlertService.error(error.data.message);
                //$state.reload();
            }
        }

        vm.loadCbBinderPercGran = loadCbBinderPercGran;
        function loadCbBinderPercGran() {
            Campione.getCbBinderPercGran({
                id: vm.campioneSelected.id
            }, onSuccess, onError);
            function onSuccess(data, headers) {
                var ghiaiaPerc = $window.Math.round(data.ghiaiaPerc),
                    sabbiaPerc = $window.Math.round(data.sabbiaPerc),
                    //limoArgillaPerc = 100 - (ghiaiaPerc + sabbiaGrossaPerc + sabbiaPerc);
                    limoArgillaPerc = $window.Math.round(data.limoArgillaPerc);

                var cheat = 0;
                if((ghiaiaPerc + sabbiaPerc + limoArgillaPerc) > 100 && limoArgillaPerc > 0) {
                    cheat = (ghiaiaPerc + sabbiaPerc + limoArgillaPerc) - 100;
                    sabbiaPerc = sabbiaPerc - cheat;
                }

                vm.cbBinderPercGranData = [
                    {
                        label: "Ghiaia",
                        //value: vm.campioneSelectedClassGeotec.ghiaiaPerc,
                        value: ghiaiaPerc,
                        color: '#2157b4'
                    },
                    {
                        label: "Sabbia",
                        //value: vm.campioneSelectedClassGeotec.sabbiaPerc,
                        value: sabbiaPerc,
                        color: '#b03434'
                    },
                    {
                        label: "Limo e Argilla",
                        //value: vm.campioneSelectedClassGeotec.limoArgillaPerc,
                        value: limoArgillaPerc,
                        color: '#75b44f'
                    }
                ];
            }
            function onError(error) {
                AlertService.error(error.data.message);
                //$state.reload();
            }
        }

        vm.options = {
            chart: {
                type: 'pieChart',
                height: 300,
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showLabels: true,
                duration: 500,
                labelThreshold: 0.01,
                labelType: "percent",
                //labelSunbeamLayout: true,
                legend: {
                    margin: {
                        top: 5,
                        right: 35,
                        bottom: 5,
                        left: 0
                    }
                },
                valueFormat: d3.format(',.0d')
            }
        };

        function updateGeotecData() {
            // visto che Excel bara con gli arrotondamenti della piechart, lo faccio pure io
            var ghiaiaPerc = $window.Math.round(vm.campioneSelectedClassGeotec.ghiaiaPerc),
                sabbiaGrossaPerc = $window.Math.round(vm.campioneSelectedClassGeotec.sabbiaGrossaPerc),
                sabbiaPerc = $window.Math.round(vm.campioneSelectedClassGeotec.sabbiaPerc),
                //limoArgillaPerc = 100 - (ghiaiaPerc + sabbiaGrossaPerc + sabbiaPerc);
                limoArgillaPerc = $window.Math.round(vm.campioneSelectedClassGeotec.limoArgillaPerc);

            var cheat = 0;
            if((ghiaiaPerc + sabbiaGrossaPerc + sabbiaPerc + limoArgillaPerc) > 100 && limoArgillaPerc > 0) {
                cheat = (ghiaiaPerc + sabbiaGrossaPerc + sabbiaPerc + limoArgillaPerc) - 100;
                sabbiaPerc = sabbiaPerc - cheat;
            }

            vm.data = [
                {
                    label: "Ghiaia",
                    //value: vm.campioneSelectedClassGeotec.ghiaiaPerc,
                    value: ghiaiaPerc,
                    color: '#2157b4'
                },
                {
                    label: "Sabbia grossa",
                    //value: vm.campioneSelectedClassGeotec.sabbiaGrossaPerc,
                    value: sabbiaGrossaPerc,
                    color: '#b03434'
                },
                {
                    label: "Sabbia",
                    //value: vm.campioneSelectedClassGeotec.sabbiaPerc,
                    value: sabbiaPerc,
                    color: '#75b44f'
                },
                {
                    label: "Limo e Argilla",
                    //value: vm.campioneSelectedClassGeotec.limoArgillaPerc,
                    value: limoArgillaPerc,
                    color: '#f2a829'
                }
            ];
        }

        vm.onCampioneSelectedChange = onCampioneSelectedChange;
        //onCampioneSelectedChange();
        function onCampioneSelectedChange() {
            if(vm.campioneSelected !== null && vm.campioneSelected.tipoCampione !== "C") {
                if(vm.campioneSelected.tipoCampione == "CB_A" || vm.campioneSelected.tipoCampione == "CB_B") {
                    loadPesataByCampioneCbUsura();
                    loadPesataByCampioneCbBinder();
                } else {
                    loadPesataByCampione();
                }

            } else {
                vm.campioneSelectedPesatas = null;
                vm.campioneSelectedCbUsuraPesatas = null;
                vm.campioneSelectedCbBinderPesatas = null;
            }
            vm.usuramedbssd = null;
            vm.usuramedpercbmisc = null;
            vm.medraggr = null;
            vm.bindermedbssd = null;
            vm.bindermedpercbmisc = null;
            vm.bindermedraggr = null;

            vm.usurapercvuoti = null;
            vm.binderpercvuoti = null;

            vm.cbUsuraPercGranData = null;
            vm.cbBinderPercGranData = null;
        }

        vm.onDeleteCurva = onDeleteCurva;
        function onDeleteCurva() {
            var modalInstance = $uibModal.open({
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                backdrop: 'static',
                templateUrl: 'app/components/dialogs/confirm-dialog-danger.html',
                controller: ['$uibModalInstance', function($uibModalInstance) {
                    var vm = this;
                    vm.msg = "Sicuro di voler eliminare la curva granulometrica?";
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
                vm.campioneSelected.curva = null;
                vm.campioneSelected.curvaContentType = null;
                Campione.update(vm.campioneSelected,
                    function(result) {
                        $scope.$emit('italgaslabApp:campioneUpdate', result);
                        $state.go($state.$current, {
                            campioneSelected: vm.campioneSelected
                        }, {reload: true});
                    },
                    function() {

                    });
            }, function(msg) {

            });
        }

        vm.onDeleteCurvaBinder = onDeleteCurvaBinder;
        function onDeleteCurvaBinder() {
            var modalInstance = $uibModal.open({
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                backdrop: 'static',
                templateUrl: 'app/components/dialogs/confirm-dialog-danger.html',
                controller: ['$uibModalInstance', function($uibModalInstance) {
                    var vm = this;
                    vm.msg = "Sicuro di voler eliminare la curva granulometrica?";
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
                vm.campioneSelected.curvaBinder = null;
                vm.campioneSelected.curvaBinderContentType = null;
                Campione.update(vm.campioneSelected,
                    function(result) {
                        $scope.$emit('italgaslabApp:campioneUpdate', result);
                        $state.go($state.$current, {
                            campioneSelected: vm.campioneSelected
                        }, {reload: true});
                    },
                    function() {

                    });
            }, function(msg) {

            });
        }

        vm.updateStatoCassetta = updateStatoCassetta;
        function updateStatoCassetta() {
            var statoAttualeCassetta = vm.cassetta.statoAttualeCassetta;
            var modalInstance = $uibModal.open({
                ariaLabelledBy: 'modal-title',
                ariaDescribedBy: 'modal-body',
                backdrop: 'static',
                templateUrl: 'app/components/dialogs/confirm-dialog-warning.html',
                controller: ['$uibModalInstance', function($uibModalInstance) {
                    var vm = this;
                    vm.msg = "Impostare lo stato della cassetta a \"" + statoAttualeCassetta + "\"?";
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
                Cassetta.update(vm.cassetta,
                    function(result) {
                        $scope.$emit('italgaslabApp:cassettaUpdate', result);
                        //$state.reload();
                    },
                    function(error) {
                        AlertService.error(error.data.message);
                    });
            }, function(msg) {

            });
        }

        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.clear = clear;
        vm.search = search;
        vm.loadCampioneByCassetta = loadCampioneByCassetta;
        vm.searchQuery = pagingParams.search;
        vm.currentSearch = pagingParams.search;
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;

        loadCampioneByCassetta();

        function loadCampioneByCassetta () {
            if (pagingParams.search) {
/////////////// Non serve la ricerca dei campioni ma va sistemato
                CampioneSearch.query({
                    query: pagingParams.search,
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort()
                }, onSuccess, onError);
            } else {
                CampioneByCassetta.query({
                    id: $stateParams.idCassetta,
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort()
                }, onSuccess, onError);
            }
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.campiones = data;
                vm.page = pagingParams.page;
                if($stateParams.campioneSelected === null && vm.campiones.length > 0) {
                    vm.campioneSelected = vm.campiones[0];
                }
                onCampioneSelectedChange();
                updateNumCampioniLavorati();
                updateProgressBarType();
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadPage (page) {
            vm.page = page;
            vm.transition();
        }

        function transition () {
            $state.transitionTo($state.$current, {
                id: $stateParams.idCassetta,
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }

        function search (searchQuery) {
            if (!searchQuery){
                return vm.clear();
            }
            vm.links = null;
            vm.page = 1;
            vm.predicate = '_score';
            vm.reverse = false;
            vm.currentSearch = searchQuery;
            vm.transition();
        }

        function clear () {
            vm.links = null;
            vm.page = 1;
            vm.predicate = 'id';
            vm.reverse = true;
            vm.currentSearch = null;
            vm.transition();
        }

    }
})();
