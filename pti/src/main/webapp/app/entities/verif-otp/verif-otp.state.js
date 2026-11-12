(function() {
    'use strict';

    angular
        .module('ptiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('verif-otp', {
            parent: 'entity',
            url: '/verif-otp?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.verifOtp.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/verif-otp/verif-otps.html',
                    controller: 'VerifOtpController',
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
                    $translatePartialLoader.addPart('verifOtp');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('verif-otp-detail', {
            parent: 'verif-otp',
            url: '/verif-otp/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ptiApp.verifOtp.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/verif-otp/verif-otp-detail.html',
                    controller: 'VerifOtpDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('verifOtp');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'VerifOtp', function($stateParams, VerifOtp) {
                    return VerifOtp.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'verif-otp',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('verif-otp-detail.edit', {
            parent: 'verif-otp-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/verif-otp/verif-otp-dialog.html',
                    controller: 'VerifOtpDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['VerifOtp', function(VerifOtp) {
                            return VerifOtp.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('verif-otp.new', {
            parent: 'verif-otp',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/verif-otp/verif-otp-dialog.html',
                    controller: 'VerifOtpDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                otp: null,
                                login: null,
                                hostId: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('verif-otp', null, { reload: 'verif-otp' });
                }, function() {
                    $state.go('verif-otp');
                });
            }]
        })
        .state('verif-otp.edit', {
            parent: 'verif-otp',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/verif-otp/verif-otp-dialog.html',
                    controller: 'VerifOtpDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['VerifOtp', function(VerifOtp) {
                            return VerifOtp.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('verif-otp', null, { reload: 'verif-otp' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('verif-otp.delete', {
            parent: 'verif-otp',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/verif-otp/verif-otp-delete-dialog.html',
                    controller: 'VerifOtpDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['VerifOtp', function(VerifOtp) {
                            return VerifOtp.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('verif-otp', null, { reload: 'verif-otp' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
