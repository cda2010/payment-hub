(function () {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('operation', {
                parent: 'entity',
                url: '/operation?page&sort&search',
                data: {
                    authorities: ['ROLE_CONSULT_OPERATION'],
                    pageTitle: 'ptiApp.operation.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/operations.html',
                        controller: 'OperationController',
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
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    validation: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    }
                }
            }).state('traitement-op', {
                parent: 'entity',
                url: '/traitement-operation?page&sort&search',
                data: {
                    authorities: ['ROLE_DO_OP_VALIDATION'],
                    pageTitle: 'ptiApp.operation.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/operations.html',
                        controller: 'OperationController',
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
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    typeService: function () {
                        return "";
                    },
                    validation: function () {
                        return true;
                    },
                    $uibModalInstance: function () {

                    }
                }
            }).state('traitement-APPELFOND', {
                parent: 'entity',
                url: '/traitement-mise-à-disposition?page&sort&search',
                data: {
                    authorities: ['ROLE_DO_OP_MAD_VALIDATION'],
                    pageTitle: 'ptiApp.operation.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/traitement-operations.html',
                        controller: 'TraitementOperationController',
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
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    typeService: function () {
                        return "ACCOUNT_CASH";
                    }
                }
            }).state('traitement-virement', {
                parent: 'entity',
                url: '/traitement-virement?page&sort&search',
                data: {
                    authorities: ['ROLE_DO_OP_VIREMENT_VALIDATION'],
                    pageTitle: 'ptiApp.operation.home.title'
                },
                views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/traitement-operations.html',
                        controller: 'TraitementOperationController',
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
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    typeService: function () {
                        return "ACCOUNT_ACCOUNT";
                    }
                }
            }).state('operation-envoi-cash', {
                parent: 'entity',
                url: '/operation-envoi-cash',
                data: {
                    authorities: ['ROLE_DO_OP_ENVOI_CASH']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/envoi_cash/init-operation.html',
                        controller: 'OperationDialogController',
                        controllerAs: 'vm',
                    }
                }, resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    isExpired: function () {
                        return false;
                    },
                    isMaj: function () {
                        return false;
                    },
                    isChange: function () {
                    return false;
                     },
                    $uibModalInstance: function () {

                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            }).state('operation-envoi-cash-to-account', {
                parent: 'entity',
                url: '/operation-envoi-cash-to-account',
                data: {
                    authorities: ['ROLE_DO_OP_CASH_ACCOUNT']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/cash-to-account/cash-to-account-dialog.html',
                        controller: 'CashToAccountDialogController',
                        controllerAs: 'vm',
                    }
                }, resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: 0,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    isMaj: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            }).state('operation-retrait', {
                parent: 'entity',
                url: '/operation-retrait',
                data: {
                    authorities: ['ROLE_DO_OP_RETRAIT']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/retrait/retrait-dialog.html',
                        controller: 'RetraitDialogController',
                        controllerAs: 'vm',
                    }
                }, resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    $uibModalInstance: function () {
                    },
                    isMaj: function () {
                        return false;
                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            }).state('creation-monnaie', {
                parent: 'entity',
                url: '/creation-monnaie',
                data: {
                    authorities: ['ROLE_CREA_MONNAIE']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/creation-monnaie/creation-monnaie.html',
                        controller: 'CreationMonnaieController',
                        controllerAs: 'vm',
                    }
                },
                resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            uoAcquereurId: null,
                            canalLibelleCanal: null,
                            code:null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
                .state('destruction-monnaie', {
                parent: 'entity',
                url: '/destruction-monnaie',
                data: {
                    authorities: ['ROLE_DEST_MONNAIE']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/destruction-monnaie/destruction-monnaie.html',
                        controller: 'DestructionMonnaieController',
                        controllerAs: 'vm',
                    }
                },
                resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            uoAcquereurId: null,
                            canalLibelleCanal: null,
                            code:null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })    
                .state('approvisionnement', {
                parent: 'entity',
                url: '/approvisionnement-compte',
                data: {
                    authorities: ['ROLE_DO_OP_APPROV']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/approvisionnement/approvisionnement.html',
                        controller: 'ApprovisionnementController',
                        controllerAs: 'vm',
                    }
                },
                resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            uoAcquereurId: null,
                            canalLibelleCanal: null,
                            code:null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            }).state('account-cash', {
                parent: 'entity',
                url: '/account-cash',
                data: {
                    authorities: ['ROLE_DO_OP_MAD_INITIATION']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/account-cash/account-cash.html',
                        controller: 'AccountCashController',
                        controllerAs: 'vm',
                    }
                }, resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    typeService: function () {
                        return "ACCOUNT_CASH";
                    },
                    isMaj: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            }).state('account-account', {
                parent: 'entity',
                url: '/account-account',
                data: {
                    authorities: ['ROLE_DO_OP_VIREMENT_INITIATION']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/account-cash/account-cash.html',
                        controller: 'AccountCashController',
                        controllerAs: 'vm',
                    }
                }, resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    typeService: function () {
                        return "ACCOUNT_ACCOUNT";
                    },
                    isMaj: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    },
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('operation');
                        $translatePartialLoader.addPart('statutOperation');
                        $translatePartialLoader.addPart('personnePhysique');
                        $translatePartialLoader.addPart('personneMorale');
                        $translatePartialLoader.addPart('sexe');
                        $translatePartialLoader.addPart('piece');
                        $translatePartialLoader.addPart('personne');
                        $translatePartialLoader.addPart('typePersonne');
                        $translatePartialLoader.addPart('compte');
                        $translatePartialLoader.addPart('compteBanque');
                        $translatePartialLoader.addPart('compteAutre');
                        $translatePartialLoader.addPart('client');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            }).state('annulation-operation', {
                parent: 'entity',
                url: '/annulation-operation',
                data: {
                    authorities: ['ROLE_ANNULATION_ENVOI_CASH']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/operation/annulation/verif_operation.html',
                        controller: 'VerifOperationController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'md',
                        resolve: {
                            translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                                $translatePartialLoader.addPart('operation');
                                $translatePartialLoader.addPart('statutOperation');
                                $translatePartialLoader.addPart('personnePhysique');
                                $translatePartialLoader.addPart('personneMorale');
                                $translatePartialLoader.addPart('sexe');
                                $translatePartialLoader.addPart('piece');
                                $translatePartialLoader.addPart('personne');
                                $translatePartialLoader.addPart('typePersonne');
                                $translatePartialLoader.addPart('compte');
                                $translatePartialLoader.addPart('compteBanque');
                                $translatePartialLoader.addPart('compteAutre');
                                $translatePartialLoader.addPart('client');
                                $translatePartialLoader.addPart('global');
                                return $translate.refresh();
                            }]
                        }
                    }).result.then(function () {
                        $state.go('home');
                    }, function () {
                        $state.go('home');
                    });
                }]
            }).state('operation-maj', {
                parent: 'entity',
                url: '/operation_maj',
                data: {
                    authorities: ['ROLE_BLOQUAGE_OP']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/operation/annulation/maj_operation.html',
                        controller: 'VerifOperationController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'md',
                        resolve: {
                            translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                                $translatePartialLoader.addPart('operation');
                                $translatePartialLoader.addPart('statutOperation');
                                $translatePartialLoader.addPart('personnePhysique');
                                $translatePartialLoader.addPart('personneMorale');
                                $translatePartialLoader.addPart('sexe');
                                $translatePartialLoader.addPart('piece');
                                $translatePartialLoader.addPart('personne');
                                $translatePartialLoader.addPart('typePersonne');
                                $translatePartialLoader.addPart('compte');
                                $translatePartialLoader.addPart('compteBanque');
                                $translatePartialLoader.addPart('compteAutre');
                                $translatePartialLoader.addPart('client');
                                $translatePartialLoader.addPart('global');
                                return $translate.refresh();
                            }]
                        }
                    }).result.then(function () {
                        $state.go('home');
                    }, function () {
                        $state.go('home');
                    });
                }]
            }).state('operation-maj-af', {
                parent: 'entity',
                url: '/operation_maj-appel-fond',
                data: {
                    authorities: ['ROLE_ANNULATION_APPEL_FOND']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/operation/annulation/maj_operation.html',
                        controller: 'VerifOperationController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'md',
                        resolve: {
                            translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                                $translatePartialLoader.addPart('operation');
                                $translatePartialLoader.addPart('statutOperation');
                                $translatePartialLoader.addPart('personnePhysique');
                                $translatePartialLoader.addPart('personneMorale');
                                $translatePartialLoader.addPart('sexe');
                                $translatePartialLoader.addPart('piece');
                                $translatePartialLoader.addPart('personne');
                                $translatePartialLoader.addPart('typePersonne');
                                $translatePartialLoader.addPart('compte');
                                $translatePartialLoader.addPart('compteBanque');
                                $translatePartialLoader.addPart('compteAutre');
                                $translatePartialLoader.addPart('client');
                                $translatePartialLoader.addPart('global');
                                return $translate.refresh();
                            }]
                        }
                    }).result.then(function () {
                        $state.go('home');
                    }, function () {
                        $state.go('home');
                    });
                }]
            }).state('simulation-operation', {
                parent: 'entity',
                url: '/operation-simulation',
                data: {
                    authorities: ['ROLE_CONSULT_SIMULATEUR']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/operations-utils.html',
                        controller: 'OperationOutilsController',
                        controllerAs: 'vm',
                    }
                }, resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    isExpired: function () {
                        return false;
                    },
                    isMaj: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    }
                }
            }).state('retrait-membre', {
                parent: 'entity',
                url: '/retrait-membre',
                data: {
                    authorities: ['ROLE_DO_OP_RETRAIT']
                }, views: {
                    'content@app': {
                        templateUrl: 'app/entities/operation/retrait-membre/retrait-membre.html',
                        controller: 'RetraitMembreController',
                        controllerAs: 'vm',
                    }
                },
                resolve: {
                    entity: function () {
                        return {
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            uoAcquereurId: null,
                            canalLibelleCanal: null,
                            code:null,
                            id: null
                        };
                    },
                    isViewing: function () {
                        return false;
                    },
                    $uibModalInstance: function () {

                    }
                }
            }).state('point-vente-inactif', {
            parent: 'entity',
            url: '/point-de-vente-inactif',
            data: {
                authorities: [],
                pageTitle: 'ptiApp.compte.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/operation/point-vente/points-vente-inactifs.html',
                    controller: 'PointsVenteInactifsController',
                    controllerAs: 'vm'
                }
            },
            params: {},
            resolve: {}
        }).state('point-vente-non-active', {
            parent: 'entity',
            url: '/point-de-vente-non-active',
            data: {
                authorities: [],
                pageTitle: 'ptiApp.compte.home.title'
            },
            views: {
                'content@app': {
                    templateUrl: 'app/entities/operation/point-vente/points-vente-non-actives.html',
                    controller: 'PointsVenteNonActivesController',
                    controllerAs: 'vm'
                }
            },
            params: {},
            resolve: {}
        }).state('operation-redressement', {
                parent: 'entity',
                url: '/operation-redressement',
                data: {
                    authorities: ['ROLE_DO_OP_REDRESSEMENT']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/operation/annulation/maj_operation.html',
                        controller: 'VerifOperationController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'md',
                        resolve: {
                            translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                                $translatePartialLoader.addPart('operation');
                                $translatePartialLoader.addPart('statutOperation');
                                $translatePartialLoader.addPart('personnePhysique');
                                $translatePartialLoader.addPart('personneMorale');
                                $translatePartialLoader.addPart('sexe');
                                $translatePartialLoader.addPart('piece');
                                $translatePartialLoader.addPart('personne');
                                $translatePartialLoader.addPart('typePersonne');
                                $translatePartialLoader.addPart('compte');
                                $translatePartialLoader.addPart('compteBanque');
                                $translatePartialLoader.addPart('compteAutre');
                                $translatePartialLoader.addPart('client');
                                $translatePartialLoader.addPart('global');
                                return $translate.refresh();
                            }]
                        }
                    }).result.then(function () {
                        $state.go('home');
                    }, function () {
                        $state.go('home');
                    });
                }]
            }).state('change-staut-operation',{
            parent: 'entity',
            url: '/change-statut-operation',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal',function ($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/operation/change-statut-operation/change-statut-operation.html',
                    controller: 'ChangeStatutOperationController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                            $translatePartialLoader.addPart('operation');
                            $translatePartialLoader.addPart('statutOperation');
                            $translatePartialLoader.addPart('personnePhysique');
                            $translatePartialLoader.addPart('personneMorale');
                            $translatePartialLoader.addPart('sexe');
                            $translatePartialLoader.addPart('piece');
                            $translatePartialLoader.addPart('personne');
                            $translatePartialLoader.addPart('typePersonne');
                            $translatePartialLoader.addPart('compte');
                            $translatePartialLoader.addPart('compteBanque');
                            $translatePartialLoader.addPart('compteAutre');
                            $translatePartialLoader.addPart('client');
                            $translatePartialLoader.addPart('global');
                            return $translate.refresh();
                        }]
                    }
                }).result.then(function () {
                    $state.go('home');
                }, function () {
                    $state.go('home');
                })

            }]
        });

    }

})();
