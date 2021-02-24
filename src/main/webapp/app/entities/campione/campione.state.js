(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('campione', {
            parent: 'entity',
            url: '/campione?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.campione.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/campione/campiones.html',
                    controller: 'CampioneController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('campione');
                    $translatePartialLoader.addPart('tipoCampione');
                    $translatePartialLoader.addPart('proceduraDetMassaVol');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('campione-detail', {
            parent: 'entity',
            url: '/campione/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'italgaslabApp.campione.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/campione/campione-detail.html',
                    controller: 'CampioneDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('campione');
                    $translatePartialLoader.addPart('tipoCampione');
                    $translatePartialLoader.addPart('proceduraDetMassaVol');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Campione', function($stateParams, Campione) {
                    return Campione.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('campione.new', {
            parent: 'campione',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/campione/campione-dialog.html',
                    controller: 'CampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                codiceCampione: null,
                                siglaCampione: null,
                                tipoCampione: null,
                                descrizioneCampione: null,
                                fotoSacchetto: null,
                                fotoSacchettoContentType: null,
                                fotoCampione: null,
                                fotoCampioneContentType: null,
                                dataAnalisi: null,
                                ripartizioneQuartatura: null,
                                essiccamento: null,
                                setacciaturaSecco: null,
                                lavaggioSetacciatura: null,
                                essiccamentoNumTeglia: null,
                                essiccamentoPesoTeglia: null,
                                essiccamentoPesoCampioneLordoIniziale: null,
                                essiccamentoPesoCampioneLordo24H: null,
                                essiccamentoPesoCampioneLordo48H: null,
                                sabbia: null,
                                ghiaia: null,
                                materialeRisultaVagliato: null,
                                detriti: null,
                                materialeFine: null,
                                materialeOrganico: null,
                                elementiMagg125Mm: null,
                                detritiConglomerato: null,
                                argilla: null,
                                argillaMatAlterabile: null,
                                granuliCementati: null,
                                elementiArrotondati: null,
                                elementiSpigolosi: null,
                                sfabbricidi: null,
                                tipoBConforme: null,
                                cbSpessoreTappetino: null,
                                cbMassaFiller: null,
                                cbMassaMaterialeEstratto: null,
                                cbMassaInterti: null,
                                cbMassaMiscela: null,
                                cbMassaBitume: null,
                                cbPercBitumeRispInterti: null,
                                cbPercBitumeRispMiscela: null,
                                cbProcDetMassaVol: null,
                                cbProv1MassaSecca: null,
                                cbProv1LegantePerc: null,
                                cbProv1MassaVolLegante: null,
                                cbProv1MassaInertiPerc: null,
                                cbProv1MassaVolInerti: null,
                                cbProv1MassaVolParaffina: null,
                                cbProv1MassaVolMax: null,
                                cbProv1MassaVolBulk: null,
                                cbProv1PercVuoti: null,
                                cbProv2MassaSecca: null,
                                cbProv2LegantePerc: null,
                                cbProv2MassaVolLegante: null,
                                cbProv2MassaInertiPerc: null,
                                cbProv2MassaVolInerti: null,
                                cbProv2MassaVolParaffina: null,
                                cbProv2MassaVolMax: null,
                                cbProv2MassaVolBulk: null,
                                cbProv2PercVuoti: null,
                                cbProv3MassaSecca: null,
                                cbProv3LegantePerc: null,
                                cbProv3MassaVolLegante: null,
                                cbProv3MassaInertiPerc: null,
                                cbProv3MassaVolInerti: null,
                                cbProv3MassaVolParaffina: null,
                                cbProv3MassaVolMax: null,
                                cbProv3MassaVolBulk: null,
                                cbProv3PercVuoti: null,
                                cbPercMediaVuoti: null,
                                curva: null,
                                curvaContentType: null,
                                classificazioneGeotecnica: null,
                                note: null,
                                lavorazioneConclusa: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('campione', null, { reload: true });
                }, function() {
                    $state.go('campione');
                });
            }]
        })
        .state('campione.edit', {
            parent: 'campione',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/campione/campione-dialog.html',
                    controller: 'CampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('campione.delete', {
            parent: 'campione',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/campione/campione-delete-dialog.html',
                    controller: 'CampioneDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
