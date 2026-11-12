(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UserManagementController', UserManagementController);

    UserManagementController.$inject = ['Profil','$rootScope', 'ChoisirUo', '$scope','Notification','BASE_URL_API','Utils' ,'Principal','RequestUtils', 'User', 'TableService'];

    function UserManagementController(Profil,$rootScope, ChoisirUo, $scope,Notification,BASE_URL_API,Utils ,Principal,RequestUtils, User, TableService) {
        var vm = this;
        var URL = BASE_URL_API + "etat/export/listing/users";
        vm.profils=Profil.queryOptimise();
        console.log(vm.profils);
        vm.currentAccount = null;
        vm.genererListe=genererListe;
        vm.setActive = setActive;

        vm.filtrer = false;
        vm.ctrlExport=false;

        vm.itemSearch = {
            "organisationId": null,
            "profilId": null,
            "login": null,
            "telephone": null
        };

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "organisationId": null,
                    "profilId": null,
                    "login": null,
                    "telephone": null
                };
                vm.organisation=null;
                init();
            }
        };

        vm.organisation = null;
        vm.choisirUo = function () {
            console.log("choisir uo");
            ChoisirUo.open($scope);
        };

        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            console.log("uo choisi");
            if (result != null) {
                vm.organisation = result;
                vm.itemSearch.organisationId = result.id;
            }
        });

        $scope.$on('$destroy', unsubscribe);


        vm.filtre = function () {
            vm.ctrlExport=true;
            console.log("filtre des informations en cours",vm.itemSearch.login);
            init();
        };

        function genererListe() {
            console.log('format',vm.format);
            vm.itemSearch.reportFormat = vm.format;
            console.log('data',vm.itemSearch);
            if(vm.format===null || vm.format==='' || vm.format=== undefined){
                console.log('f is null',vm.format);
                vm.itemSearch.reportFormat = 'PDF';
            }
            if(vm.itemSearch.reportFormat ==='PDF')
            {
                Utils.postPrintUrl(URL,vm.itemSearch);
            } else {
                if(vm.itemSearch.reportFormat ==='XLSX') {console.log('oui') ; vm.name='liste_users.xlsx';}
                if(vm.itemSearch.reportFormat ==='CSV') {console.log('oui') ;vm.name='liste_users.csv';}
                Utils.downloadFile(URL,vm.itemSearch,vm.name);
            }

        }


        Principal.identity().then(function (account) {
            vm.currentAccount = account;
        });

        function setActive(user, isActivated) {
            console.log('user',user);
            user.activated = isActivated;
            user.nbrFailure=0;
            var info;
            if(isActivated){
                 info='Activé';
            } else {
                 info='Désactivé';
            }
            User.update(user, function (result) {
                vm.loadAll();
                vm.clear();
                Notification.success('Utilisateur'+ ' '+ user.login+' '+info);
                console.log(result);
            }, function () {
                user.activated = !user.activated;
            });
        }

        init();

        function init() {
            console.log("filtre en cours***");
            var addItemData = {
                templateUrl: 'app/admin/user-management/user-management-dialog.html',
                controller: 'UserManagementDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {
                            login: null,
                            email: null,
                            id: null
                        };
                    }
                }
            };

            /**
             *Modification d'un élément
             */
            var editItemData = {
                templateUrl: 'app/admin/user-management/user-management-dialog.html',
                controller: 'UserManagementDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return true;
                    },
                    entity: null
                }
            };

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {
                templateUrl: 'app/admin/user-management/user-management-delete-dialog.html',
                controller: 'UserManagementDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };

            var detailItemData = {
                templateUrl: 'app/admin/user-management/user-management-dialog.html',
                controller: 'UserManagementDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return true;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };

            var entityParams = function () {
                return {
                    "profilId": vm.itemSearch.profilId,
                    "uoId": vm.itemSearch.organisationId,
                    "login": vm.itemSearch.login,
                    "telephone": vm.itemSearch.telephone
                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity: RequestUtils.UserOptimise,
                entitySearch: RequestUtils.UserOptimise,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            console.log('user',datas);
        }

        vm.setItemsPerPage(10);
    }
})();

