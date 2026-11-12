(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MachineController', MachineController);

    MachineController.$inject = ['$uibModal','Notification','$http','RequestUtils','Utils', 'CancelAlert', 'TableService', '$scope', 'Machine'];

    function MachineController($uibModal,Notification,$http,RequestUtils,Utils, CancelAlert, TableService, $scope, Machine) {
        var vm = this;

        vm.activateItem = activateItem;
        vm.downloadCert = downloadCert;
        vm.allowItem=allowItem;
        vm.successMsg = '';
        vm.errorMsg = '';
        vm.filtreMachine= filtreMachine;
        vm.filtrer=true;
        vm.doFiltre=false;
        vm.itemSearch = {
            "adresse": null
        };
        init();
        function activateItem($event, item) {
            vm.successMsg = '';
            vm.errorMsg = '';
            CancelAlert.confirm(function (arg) {
                item.cle=arg;
                Machine.update(item, function (data) {
                    vm.successMsg = "Le poste de travail de " + item.nomDemandeur + " " + item.prenomDemandeur + " a été activée. Récupérez le certificat pour le lui envoyer. Contactez l'administrateur pour suivre les procédures";
                    vm.errorMsg = "";
                    downloadCert(null, item);
                    vm.loadAll();
                }, function (err) {
                    vm.successMsg = "";
                    vm.errorMsg = "L'activation de l'ordinateur n'a pas pu s'effectuer. Ressayer plus tard, si cela persiste, contacter l'administrateur";
                });
            }, function (arg) {
                // body...
            }, 'Etes vous certains de vouloir activer cet poste de travail ?',true);
        };

        function downloadCert($event, item) {
            vm.successMsg = '';
            vm.errorMsg = '';
            CancelAlert.confirm(function (arg) {
                Utils.downloadFileWithGet('api/machines/' + item.uuid, {
                    format: 'octet-stream'
                }, item.currentUser+".p12");
            }, function (arg) {
                // body...
            }, 'Confirmez-vous l\'envoi du certificat?');
        };
        /**
         * Suppresion d'une machine
         */
        function filtreMachine(){
            vm.doFiltre=true;
            vm.filtrer=false;
        }

        function allowItem($event,machine) {
            console.log('test',machine);
/*            vm.successMsg = '';
            vm.errorMsg = '';
            CancelAlert.confirm(function (arg) {
                machine.cle=arg;
                Machine.allow({uuid:machine.uuid}, function (data) {
                    vm.successMsg = "Le poste de travail de " + machine.nomDemandeur + " " + machine.prenomDemandeur + " a été activée. Récupérez le certificat pour le lui envoyer. Contactez l'administrateur pour suivre les procédures";
                    vm.errorMsg = "";
                    downloadCert(null, machine);
                    vm.loadAll();
                }, function (err) {
                    vm.successMsg = "";
                    vm.errorMsg = "L'activation de l'ordinateur n'a pas pu s'effectuer. Ressayer plus tard, si cela persiste, contacter l'administrateur";
                });
            }, function (arg) {
                // body...
            }, 'Etes vous certains de vouloir activer cet poste de travail ?',true);
       */
            Machine.allow({uuid:machine.uuid},function (result) {
                console.log(result)
            })
        }

        vm.initFiltre = function () {
            vm.doFiltre=false;
            vm.filtrer=true;
                vm.itemSearch = {
                    "adresse": null
                };

        };

        vm.filtre = function () {
            console.log("filtre des informations en cours",vm.itemSearch.adresse);
            init();
        };

        function init() {

        var deleteItemData = {
            templateUrl: 'app/entities/machine/machine-delete-dialog.html',
            controller: 'MachineDeleteController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                entity: null
            }
        };
        var addItemData = {
            templateUrl: 'app/entities/machine/machine-register.html',
            controller: 'MachineDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function() {
                    return {
                        organisationId: null,
                        detailsMachineContentType:null,
                        detailsMachine:null
                    };
                }
            }
        };

        var dialogData = {
            addData: addItemData,
            editData: {},
            deleteData: deleteItemData
        };

        var entitySearchParams = function () {
            return {};
        };

        var entityParams = function () {
            return {
                "adresse": vm.itemSearch.adresse
            };
        };
        var datas = {
            scope: $scope,
            vm: vm,
            //entity: Machine,
            entity: RequestUtils.GetListMachineOptimise,
            //entitySearch: Machine,
            entitySearch: RequestUtils.GetListMachineOptimise,
            dialogData: dialogData,
            entitySearchParams: entitySearchParams,
            entityParams: entityParams
        };
        TableService.init(datas);
        }
        //Verification de token
        vm.token=null;
        vm.startVerif= function startVerif() {
            var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
            $http.jsonp(url)
                .success(function (data, status, headers, config) {
                    data.login = angular.copy(vm.username);
                    RequestUtils.VerifToken.get({
                        token: vm.token ,
                        loginData: JSON.stringify(data)
                    }, function (datas) {
                       Notification.success("Token valide!");
                    });
                }).error(function (data, status, headers, config) {
                Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
            });
        };

        vm.auditEvent=function(machineId){
            $uibModal.open({
                templateUrl: 'app/components/tables/tables.html',
                controller: 'MachineAuditEventController',
                controllerAs: 'vm',
                size: 'lg',
                resolve: {
                    machineId:machineId
                }
            })
        };

        vm.editItem=function (machine) {
            $uibModal.open({
                templateUrl: 'app/entities/machine/machine-update.html',
                controller: 'MachineDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: machine
                }
            })
        }
        vm.deleteMachine=function (machine) {
            console.log('oo---',machine);
            $uibModal.open({
                templateUrl: 'app/entities/machine/machine-delete-dialog-machine.html',
                controller: 'MachineDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['Machine', function (Machine) {
                        return machine;
                    }]
                }
            }).result.then(function () {
                init();
                Notification.success("Suppression effectué !");
            }, function () {
            });
        };
    }
})();
