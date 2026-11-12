(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationController', OperationController);

    OperationController.$inject = ['$window','BASE_URL','Utils','$rootScope','ChoisirUo','validation','Notification', 'CancelAlert', '$uibModal', 'RequestUtils', 'Canal', 'TypeService', 'TableService', '$scope', '$state', 'Operation','$stateParams','Utils'];

    function OperationController($window,BASE_URL,choisirUoUtils,$rootScope,ChoisirUo,validation,Notification, CancelAlert, $uibModal, RequestUtils, Canal, TypeService, TableService, $scope, $state, Operation,$stateParams,Utils) {

        var vm = this;
        vm.validation=validation;
        vm.personneId=$stateParams.personneId;
        console.log('gfdgf',vm.personneId);
        vm.format=null;
        vm.total=0;
        var URL = BASE_URL + "api/reporting/operations/bordereau";
        var URL2 = BASE_URL + "api/reporting/operations/listing";
        console.log('vm.validation',vm.validation);
        if(vm.validation){
            vm.statuts = ["ENATTENTE_VALIDATION","ENATTENTE_ANNULATION" ];
            vm.viewAnnulation=true;
        }else{
            vm.statuts = ["PAYE", "ENATTENTE","ENATTENTE_VALIDATION", "ANNULLE", "PAYE", "SUSPENDU", "REJETE", "VALIDE", "REMBOURSE","AMBIGUE"];
            vm.viewAnnulation=false;
        }
        
        vm.typeServices = TypeService.queryAllOptimise();
        vm.canals = Canal.query();
        vm.filtrer = false;
        vm.itemSearch = {
            "statutOperation": (vm.validation===true)? "ENATTENTE_VALIDATION": null,
            "statutAnnuler": (vm.validation === true),
            "canalId": null,
            "typeServiceId": null,
            "montantOp": null,
            "dateOperation": null,
            "uoAcquereurId": null,
            "dateFin": null,
            "codeOperation":null,
            "description":null,
            "personneId":vm.personneId
        };
        vm.item = {
            statutOperation: null,
            canalId: null,
            typeServiceId: null,
            montantOp: null,
            dateOperation: null,
            uoAcquereurId: null,
            dateFin: null,
            codeOperation: null,
            description: null,
            organisationId: null,
            personneId: null
        };

        vm.exportAll= function exportAll() {
       /*     RequestUtils.ExportCsv.get({
                "statutOperation": vm.itemSearch.statutOperation,
                "canalId": vm.itemSearch.canalId,
                "typeServiceId": vm.itemSearch.typeServiceId,
                "montantOp": vm.itemSearch.montantOp,
                "dateOperation": vm.itemSearch.dateOperation,
                "dateFin": vm.itemSearch.dateFin,
                "uoAcquereurId": vm.itemSearch.uoAcquereurId,
                "codeOperation": vm.itemSearch.codeOperation,
                "personneId": vm.itemSearch.personneId,
                "organisationId": vm.organisation.id
            }, function (datas) {
               Notification.success("Generation effectuée avec succès!Fichier disponible dans votre repertoire utilisateur");
            });*/
            vm.item.statutOperation=vm.itemSearch.statutOperation;
            vm.item.canalId=vm.itemSearch.canalId;
            vm.item.typeServiceId=vm.itemSearch.typeServiceId;
            vm.item.montantOp =vm.itemSearch.montantOp;
            vm.item.dateOperation=vm.itemSearch.dateOperation;
            vm.item.dateFin=vm.itemSearch.dateFin;
            vm.item.description=vm.itemSearch.description;
            vm.item.uoAcquereurId=vm.itemSearch.uoAcquereurId;
            vm.item.codeOperation=vm.itemSearch.codeOperation;
            vm.item.personneId=vm.itemSearch.personneId;
            vm.item.organisationId = vm.organisation.id;
            vm.item.format= vm.format;
            if(vm.format===null || vm.format==='' ){
                vm.item.format = 'PDF';
            }
            console.log('f',vm.item);
            if(vm.item.format ==='PDF' || vm.item.format ===null)
            {
                Utils.postPrintUrl(URL2,vm.item);
            } else {
                if (vm.item.format === 'XLSX') {
                        vm.name = 'ExportOperation.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'ExportOperation.csv';
                    }
                Utils.downloadFile(URL2,vm.item,vm.name);
            }
        };

        vm.comptabilisationOp = function (operation) {
            CancelAlert.confirm(function () {
                RequestUtils.ComptabilisationOp.get({
                    operationId: operation.id
                }, function (datas) {
                   Notification.success("Operation validée avec succès!")
                });
            });
        };

        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "statutOperation": null,
                    "canalId": null,
                    "typeServiceId": null,
                    "montantOp": null,
                    "dateOperation": null,
                    "uoAcquereurId": null,
                    "dateFin": null,
                    "codeOperation": null,
                    "description": null
                };
                vm.organisation.id = null;
                vm.organisation.libelleUniteOrganisation = null;
                init();
            }
        };

        vm.filtre = function () {
            console.log("filtre des informations en cours");
            init();
        };

        RequestUtils.GetOrganisationByTypeOptimise.get({type: "PAYS"}, function (datas) {
            vm.pays = datas;
        });

        vm.detailsOperations = function (event, operation) {
            console.log("details des operations---*");
            $uibModal.open({
                templateUrl: 'app/entities/operation/account-cash/account-cash.html',
                controller: 'AccountCashController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    estAnnuler: false,
                    typeService: function () {
                        return operation.code;
                    },isMaj:(vm.validation==true)? true:false,
                }
            });
        };

        vm.detailsOperationsPaiements = function (event, operation) {
            console.log("details des operations des paiements");
            $uibModal.open({
                templateUrl: 'app/entities/operation/paiement/paiement.html',
                controller: 'PaiementController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    typeService: function () {
                        return operation.code;
                    },isMaj:(vm.validation==true)? true:false,
                }
            });
        };

        vm.detailsOperationsWTB = function (event, operation) {
            console.log("details des operations des paiements");
            $uibModal.open({
                templateUrl: 'app/entities/operation/paiement/wallet_bank.html',
                controller: 'PaiementController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    typeService: function () {
                        return operation.code;
                    },isMaj:(vm.validation==true)? true:false,
                }
            });
        };


        vm.detailsOperationsCashAccount = function (event,operation) {
            console.log("details des operations OpController");
            console.log(operation);
            $uibModal.open({
                templateUrl: 'app/entities/operation/cash-to-account/cash-to-account-dialog.html',
                controller: 'CashToAccountDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing:true,
                    typeService: function () {
                        return operation.code;
                    },isMaj:(vm.validation==true)? true:false
                }
            }).result.then(function () {
                //$uibModalInstance.close(true);
            });
        };




 vm.detailsRegualtion= function (event,operation) {
            console.log("details des operations OpController");
            console.log(operation);
            $uibModal.open({
                templateUrl: 'app/entities/operation/regulation/operation-regulation-validation.html',
                controller: 'CompteModalController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing:true,
                    isCanceled :true,
                    typeService: function () {
                        return operation.code;
                    },isMaj:(vm.validation==true)? true:false
                }
            }).result.then(function () {
                //$uibModalInstance.close(true);
            });
        };



        vm.detailsOperationsApprovisionnement = function (event, operation) {
            console.log("details des operations");
            $uibModal.open({
                templateUrl: 'app/entities/operation/approvisionnement/approvisionnement.html',
                controller: 'ApprovisionnementController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    typeService: function () {
                        return operation.code;
                    }
                }
            });
        };
 vm.detailsCreationMonnaie = function (event, operation) {
            console.log("details des operations");
            $uibModal.open({
                templateUrl: 'app/entities/operation/creation-monnaie/creation-monnaie.html',
                controller: 'CreationMonnaieController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    typeService: function () {
                        return operation.code;
                    },
                     isMaj:(vm.validation==true)? true:false
                }
            });
        };
 vm.detailsTransaction = function(operationId){
     console.log(operationId);
     Operation.findTransactionOptimise({operationId:operationId}, function (response) {
         console.log(response);
         if(response.length===0){
             Notification.error("Aucune transaction");
         } else {
             $uibModal.open({
                 templateUrl: 'app/entities/operation/transaction/operation-transaction.html',
                 controller: 'OpeTransactModalController',
                 controllerAs: 'vm',
                 backdrop: 'static',
                 size: 'lg',
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
                     entity: function () {
                         return operationId;
                     }
                 }
             }).result.then(function () {
                 //$uibModalInstance.close(true);
             });
         }
     })
 };
 vm.detailsCommission = function(operationId) {
    Operation.getCommission({operationId: operationId}, function (result) {
        console.log(result);
        if(result.length ===0)
        {
            Notification.error('Aucune commission trouvé');
        }
        else {
            $uibModal.open({
                templateUrl: 'app/entities/operation/commission/commission-operation.html',
                controller: 'OperationCommissionModalController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
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
                    entity: function () {
                        return operationId;
                    }
                }

            })
        }
    })
 };
        vm.print=function(id){
            Utils.postPrintUrl(URL, {
                'operationId': id
            });
        };
        vm.choisirUo = function () {
            console.log("choisir uo");
            var data = {
                "acteurs": ["FSE","AGENCE", "MASTER", "SOUS_DISTRIBUTEUR"]
            };
            ChoisirUo.open($scope,data);
        };

        vm.organisation={
            id:null
        };
        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            console.log("uo choisi");
            if (result != null) {
                vm.organisation = result;
                vm.itemSearch.organisationId = result.id;
            }
        });

        
        vm.detailsOperationsRetrait = function (event, operation) {
            console.log("details des operations");
            console.log(operation);
            $uibModal.open({
                templateUrl: 'app/entities/operation/retrait/retrait-dialog.html',
                controller: 'RetraitDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    typeService: function () {
                        return operation.code;
                    },
                    isMaj: function () {
                        return false;
                    }
                }
            });
        };

        vm.detailsOperationDiverse = function(event,operation){
            console.log("details des operations");
            console.log('---R--',operation);
            $uibModal.open({
                templateUrl: 'app/entities/operation/detail-operation/detail-operation.html',
                controller: 'DetailOperationController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    typeService: function () {
                        return operation.code;
                    }
                }
            });
        };


        vm.sendCodeRetrait = function (operation) {
            CancelAlert.confirm(function () {
                RequestUtils.OperationSendCodeRetrait.save(operation, function () {
                    console.log("code envoyé");
                    Notification.success("Code retrait renvoyé");
                }, function () {
                    vm.isSaving = false;
                });
            });
        };

        init();


$scope.$on('ptiApp:operationUpdate', function (event, data) {
      console.log('---R--');
 init(); 
});
        function init() {
            var addItemData = {
                templateUrl: 'app/entities/operation/operation-dialog.html',
                controller: 'OperationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return {
                            montantOp: null,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            id: null
                        };
                    },
                    isViewing: false,
                    isExpired: false,
                    isMaj:(vm.validation==true)? true:false
                }
            };

            /**
             *Modification d'un élément
             */
            var editItemData = {};

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {};

            var detailItemData = {
                templateUrl: 'app/entities/operation/envoi_cash/init-operation.html',
                controller: 'OperationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: null, //valeur déterminé à l'appel de la boite
                    isViewing: true,
                    isExpired: false,
                    isChange: false,
                    isMaj:(vm.validation==true)? true:false
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
                    "statutOperation": vm.itemSearch.statutOperation,
                    "statutAnnuler": vm.itemSearch.statutAnnuler,
                    "canalId": vm.itemSearch.canalId,
                    "typeServiceId": vm.itemSearch.typeServiceId,
                    "montantOp": vm.itemSearch.montantOp,
                    "dateOperation": vm.itemSearch.dateOperation,
                    "dateFin": vm.itemSearch.dateFin,
                    "uoAcquereurId": vm.itemSearch.uoAcquereurId,
                    "codeOperation": vm.itemSearch.codeOperation,
                    "personneId": vm.itemSearch.personneId,
                    "description": vm.itemSearch.description,
                    "organisationId": vm.organisation.id
                };
            };

                var datas = {
                    scope: $scope,
                    vm: vm,
                    //entity: vm.personneId!=null?RequestUtils.OperationByPersonne:Operation,
                    entity: vm.personneId!=null?RequestUtils.OperationByPersonne:RequestUtils.OperationOptimise,
                    entitySearch: vm.personneId!=null?RequestUtils.OperationByPersonne:RequestUtils.OperationOptimise,
                    dialogData: dialogData,
                    entitySearchParams: entitySearchParams,
                    entityParams: entityParams
                };
            TableService.init(datas);
            console.log(datas);
        }

        /**
         * Mise à jour d'une operation
         */
        vm.majOperation = function (event,operation) {
            CancelAlert.confirm(function () {
                console.log("Mise à jour de l'operation");
                RequestUtils.OperaionMaj.put(operation,function (operation) {
                    console.log(operation);
                    vm.loadAll();
                });
            });
        }

    }
})();
