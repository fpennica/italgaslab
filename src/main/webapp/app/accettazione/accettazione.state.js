(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('accettazione', {
            parent: 'app',
            url: '/accettazione',
            data: {
                authorities: ["ROLE_USER"]
            },
            views: {
                'content@': {
                    templateUrl: 'app/accettazione/accettazione.html',
                    controller: 'AccettazioneController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                    $translatePartialLoader.addPart('accettazione');
                    return $translate.refresh();
                }]
            }
        })
        .state('accettazione.consegna', {
            parent: 'accettazione',
            url: '/consegna?page&sort&search',
            data: {
                authorities: ['ROLE_USER']
            },
            views: {
                'content@accettazione': {
                    templateUrl: 'app/accettazione/accettazione-consegna.html',
                    controller: 'ConsegnaController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'dataConsegna,desc',
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
                    $translatePartialLoader.addPart('consegna');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('accettazione.consegna.new', {
            parent: 'accettazione.consegna',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/consegna/consegna-dialog.html',
                    controller: 'ConsegnaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nome: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('accettazione.consegna', null, { reload: true });
                }, function() {
                    $state.go('accettazione.consegna');
                });
            }]
        })
        .state('accettazione.consegna.edit', {
            parent: 'accettazione.consegna',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/consegna/consegna-dialog.html',
                    controller: 'ConsegnaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Consegna', function(Consegna) {
                            return Consegna.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('accettazione.consegna', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('accettazione.consegna.delete', {
            parent: 'accettazione.consegna',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/consegna/consegna-delete-dialog.html',
                    controller: 'ConsegnaDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Consegna', function(Consegna) {
                            return Consegna.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('accettazione.consegna', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        
        .state('accettazione.cassetta', {
            parent: 'accettazione',
            url: '/consegna/{idConsegna}/cassetta?page&sort&search',
            data: {
                authorities: ['ROLE_USER']
            },
            views: {
                'content@accettazione': {
                    templateUrl: 'app/accettazione/accettazione-cassette.html',
                    controller: 'AccettazioneCassettaController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,desc',
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
                    $translatePartialLoader.addPart('cassetta');
                    $translatePartialLoader.addPart('statoContenitore');
                    $translatePartialLoader.addPart('statoAttualeCassetta');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }],
                consegnaEntity: ['$stateParams', 'Consegna', function($stateParams, Consegna) {
                    return Consegna.get({id : $stateParams.idConsegna});
                }]
            }
        })
        .state('accettazione.cassetta.new', {
            parent: 'accettazione.cassetta',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/accettazione/cassetta-dialog-accettazione.html',
                    controller: 'CassettaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['$stateParams', 'Consegna', function($stateParams, Consegna) {
                            return {
                                codiceCassetta: null,
                                odl: null,
                                rifGeografo: null,
                                prioritario: null,
                                indirizzoScavo: null,
                                denomCantiere: null,
                                codiceRdc: null,
                                dataScavo: null,
                                coordGpsN: null,
                                coordGpsE: null,
                                coordX: null,
                                coordY: null,
                                centroOperativo: null,
                                impresaAppaltatrice: null,
                                incaricatoAppaltatore: null,
                                tecnicoItgResp: null,
                                numCampioni: null,
                                presenzaCampione10: null,
                                presenzaCampione11: null,
                                presenzaCampione12: null,
                                presenzaCampione13: null,
                                presenzaCampione14: null,
                                presenzaCampione15: null,
                                presenzaCampione16: null,
                                presenzaCampione17: null,
                                statoContenitore: null,
                                statoAttualeCassetta: "IN_LAVORAZIONE",
                                contenutoInquinato: null,
                                conglomeratoBituminoso: null,
                                fotoContenitore: null,
                                fotoContenitoreContentType: null,
                                fotoContenuto: null,
                                fotoContenutoContentType: null,
                                msSismicitaLocale: null,
                                msValAccelerazione: null,
                                note: null,
                                certificatoPdf: null,
                                certificatoPdfContentType: null,
                                numProtocolloCertificato: null,
                                id: null,
                                consegna: Consegna.get({id : $stateParams.idConsegna})
                            };
                        }]
                    }
                }).result.then(function() {
                    $state.go('accettazione.cassetta', null, { reload: true });
                }, function() {
                    $state.go('accettazione.cassetta');
                });
            }]
        })
        .state('accettazione.cassetta.edit', {
            parent: 'accettazione.cassetta',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/accettazione/cassetta-dialog-accettazione.html',
                    controller: 'CassettaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Cassetta', function(Cassetta) {
                            return Cassetta.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('accettazione.cassetta', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('accettazione.cassetta.delete', {
            parent: 'accettazione.cassetta',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/cassetta/cassetta-delete-dialog.html',
                    controller: 'CassettaDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Cassetta', function(Cassetta) {
                            return Cassetta.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('accettazione.cassetta', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('accettazione.campione', {
            parent: 'accettazione',
            url: '/cassetta/{idCassetta}/campione?page&sort&search',
            data: {
                authorities: ['ROLE_USER']
            },
            views: {
                'content@accettazione': {
                    templateUrl: 'app/accettazione/accettazione-campioni.html',
                    controller: 'AccettazioneCampioneController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'codiceCampione,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                cassetta: ['$stateParams', 'Cassetta', function($stateParams, Cassetta) {
                    return Cassetta.get({id : $stateParams.idCassetta}).$promise;
                }],
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
        .state('accettazione.campione.new', {
            parent: 'accettazione.campione',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/accettazione/campione-dialog-accettazione.html',
                    controller: 'AccettazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['$stateParams', 'Cassetta', function($stateParams, Cassetta) {
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
                                id: null,
                                cassetta: Cassetta.get({id : $stateParams.idCassetta})
                            };
                        }]
                    }
                }).result.then(function() {
                    $state.go('accettazione.campione', null, { reload: true });
                }, function() {
                    $state.go('accettazione.campione');
                });
            }]
        })
        .state('accettazione.campione.edit', {
            parent: 'accettazione.campione',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/accettazione/campione-dialog-accettazione.html',
                    controller: 'AccettazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('accettazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('accettazione.campione.delete', {
            parent: 'accettazione.campione',
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
                    $state.go('accettazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }
})();
