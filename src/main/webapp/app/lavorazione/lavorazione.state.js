(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('lavorazione', {
            parent: 'app',
            url: '/lavorazione',
            data: {
                authorities: ["ROLE_USER"]
            },
            views: {
                'content@': {
                    templateUrl: 'app/lavorazione/lavorazione.html',
                    controller: 'LavorazioneController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                    $translatePartialLoader.addPart('lavorazione');
                    return $translate.refresh();
                }]
            }
        })
        .state('lavorazione.consegna', {
            parent: 'lavorazione',
            url: '/consegna?page&sort&search',
            data: {
                authorities: ['ROLE_USER']
            },
            views: {
                'content@lavorazione': {
                    templateUrl: 'app/lavorazione/lavorazione-consegna.html',
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
        .state('lavorazione.cassetta', {
            parent: 'lavorazione',
            url: '/consegna/{idConsegna}/cassetta?page&sort&search',
            data: {
                authorities: ['ROLE_USER']
            },
            views: {
                'content@lavorazione': {
                    templateUrl: 'app/lavorazione/lavorazione-cassette.html',
                    controller: 'LavorazioneCassettaController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'statoAttualeCassetta,asc',
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
        .state('lavorazione.campione', {
            parent: 'lavorazione',
            url: '/cassetta/{idCassetta}/campione?page&sort&search',
            data: {
                authorities: ['ROLE_USER']
            },
            views: {
                'content@lavorazione': {
                    templateUrl: 'app/lavorazione/lavorazione-campioni.html',
                    controller: 'LavorazioneCampioneController',
                    controllerAs: 'vm'
                }
            },
            params: {
                campioneSelected: null,
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
                    $translatePartialLoader.addPart('cassetta');
                    $translatePartialLoader.addPart('tipoCampione');
                    $translatePartialLoader.addPart('proceduraDetMassaVol');
                    $translatePartialLoader.addPart('global');
                    $translatePartialLoader.addPart('statoAttualeCassetta');
                    return $translate.refresh();
                }]
            }
        })
        .state('lavorazione.campione.new', {
            parent: 'lavorazione.campione',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-campione-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
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
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^', {campioneSelected: null});
                });
            }]
        })
        .state('lavorazione.campione.edit', {
            parent: 'lavorazione.campione',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-campione-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.delete', {
            parent: 'lavorazione.campione',
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
                    $state.go('lavorazione.campione', {campioneSelected: null}, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.essiccamento', {
            parent: 'lavorazione.campione',
            url: '/{id}/essiccamento',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-campione-essiccamento-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.setacciatura', {
            parent: 'lavorazione.campione',
            url: '/{id}/setacciatura',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-setacciatura-dialog.html',
                    controller: 'LavorazioneSetacciaturaController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    windowClass: 'xl-modal',
                    animation: false,
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^', null, { reload: true });
                });
            }]
        })
        .state('lavorazione.campione.usura_spessore', {
            parent: 'lavorazione.campione',
            url: '/{id}/usura_spessore',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-usura-spessore-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.binder_spessore', {
            parent: 'lavorazione.campione',
            url: '/{id}/binder_spessore',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-binder-spessore-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.usura_mva', {
            parent: 'lavorazione.campione',
            url: '/{id}/usura_mva',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-usura-mva-ssd-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.binder_mva', {
            parent: 'lavorazione.campione',
            url: '/{id}/binder_mva',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-binder-mva-ssd-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.usura_legante', {
            parent: 'lavorazione.campione',
            url: '/{id}/usura_legante',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-usura-legante-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.binder_legante', {
            parent: 'lavorazione.campione',
            url: '/{id}/binder_legante',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-binder-legante-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.usura_mvaggr', {
            parent: 'lavorazione.campione',
            url: '/{id}/usura_mvaggr',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-usura-mvaggr-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.binder_mvaggr', {
            parent: 'lavorazione.campione',
            url: '/{id}/binder_mvaggr',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-binder-mvaggr-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.usura_lavaggio', {
            parent: 'lavorazione.campione',
            url: '/{id}/usura_lavaggio',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-usura-lavaggio-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.binder_lavaggio', {
            parent: 'lavorazione.campione',
            url: '/{id}/binder_lavaggio',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-cb-binder-lavaggio-dialog.html',
                    controller: 'LavorazioneCampioneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    //windowClass: 'xl-modal',
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('lavorazione.campione.setacciatura_cb_usura', {
            parent: 'lavorazione.campione',
            url: '/{id}/setacciatura_cb_usura',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-setacciatura-cb-usura-dialog.html',
                    controller: 'LavorazioneSetacciaturaCbUsuraController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    windowClass: 'xl-modal',
                    animation: false,
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^', null, { reload: true });
                });
            }]
        })
        .state('lavorazione.campione.setacciatura_cb_binder', {
            parent: 'lavorazione.campione',
            url: '/{id}/setacciatura_cb_binder',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/lavorazione/lavorazione-setacciatura-cb-binder-dialog.html',
                    controller: 'LavorazioneSetacciaturaCbBinderController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    windowClass: 'xl-modal',
                    animation: false,
                    resolve: {
                        entity: ['Campione', function(Campione) {
                            return Campione.get({id : $stateParams.id}).$promise;
                        }],
                        binder: ['$stateParams', function($stateParams){
                            return $stateParams.binder;
                        }]
                    }
                }).result.then(function() {
                    $state.go('lavorazione.campione', null, { reload: true });
                }, function() {
                    $state.go('^', null, { reload: true });
                });
            }]
        });

    }
})();
