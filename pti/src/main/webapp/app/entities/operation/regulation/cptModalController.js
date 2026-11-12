(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteModalController', CompteModalController);


    CompteModalController.$inject = ['$scope','$stateParams','$state','$uibModal','$uibModalInstance','Notification','entity','Regulation','Utils','InitSteps','OperationUtils','RequestUtils','TypeUniteOrganisation','UniteOrganisation','Operation','EtiquetteCompte','isViewing'];

    function CompteModalController($scope,$state,$stateParams,$uibModal,$uibModalInstance,Notification,entity,Regulation,Utils,InitSteps,OperationUtils,RequestUtils,TypeUniteOrganisation,UniteOrganisation,Operation,EtiquetteCompte,isViewing) {

        var vm = this;
        vm.etiq=[];
        //vm.cptSId=null;
        $scope.lib=[];
        $scope.data=[];
        $scope.test='CPTECOMACTEUR';
        $scope.obj = entity;
        $scope.ctrlSolde= false;
        vm.operation = entity;
        vm.transaction=entity;
        vm.isViewing = isViewing;
        vm.yes=[];
        $scope.listeAll=[];
        vm.operationStatut=[];
        vm.etiquetteCompte=EtiquetteCompte.queryByPTI();
        console.log('etq',vm.etiquetteCompte);
        console.log('vm operation',vm.operation);
        

        vm.init1=init1;
        vm.init2=init2;
        vm.init3=init3;
        vm.validation=validation;  
        vm.voirCommentaire=voirCommentaire;
        vm.detailsOperation=detailsOperation;
        if(vm.isViewing){
            detailsOperation();
        }
        init1();
        loadAll();
        function loadAll () {  
            Operation.getStatut({operationId:vm.operation.id}, onSuccess, onError);
            
            function onSuccess(data) {
                console.log('vm operation id',vm.operation.id);
                vm.operationStatut = data;
                console.log('operation data',data);

            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }
        function init1(){
            angular.forEach($scope.obj,function (value,key) {
                var i=0;
                if (value) {
                    if (value.etiquette !== undefined) {
                        console.log('all cpt',value);
                        if (value.etiquette.endsWith('PTI')) {
                            i += 1;
                            console.log(i);
                            //$scope.data.push(value);
                        } else {
                            if (value.valeurCompte!=null)
                            {
                                $scope.data.push(value);
                            }
                            i += 1;
                            console.log(i,'--',value.valeurCompte);
                        }
                    }
                }
            });
            angular.forEach(vm.etiquetteCompte, function (value,keys) {
               vm.yes.push(value);
            });
            console.log('modal',$scope.obj,vm.yes);
            init2();
        }
        function init2(){
            angular.forEach($scope.data,function (value,key) {
                vm.etiq.push(value.etiquette);
            });
            //console.log('after tableau',vm.voirSolde.etiq);
            init3();
        }
        function init3() {
            Regulation.getLibelleCpt({liste:vm.etiq}, function (response) {
                vm.result=response;
                console.log('papa',response);
                $scope.lib.push(vm.result);

                var i=0;
                if (response) {
                    angular.forEach(response, function (value, keys) {
                        var v={};
                        i++;
                        console.log(keys.id);
                        v.libelle=value;
                        $scope.listeAll.push(v);
                    });
                    console.log('ok',$scope.listeAll);
                }

            });
            for (let i=0;i<vm.etiq.length;i++)
            {
                vm.data=JSON.stringify(vm.etiq[i]);
                vm.test=vm.etiq[i];
                console.log('tik',vm.test,vm.data,vm.etiquetteCompte.length);
            }
            console.log('tout',$scope.lib);
            console.log('compte',$scope.data);
        }

        function detailsOperation() {
                Operation.getOneOptimise({id:vm.operation.id},function (reponse) {
                    console.log('reponse---------',reponse);
                    console.log('src',reponse.operationDTO.fseSrc);
                    console.log('dest',reponse.operationDTO.fseDest);
                })
        }

        if (!vm.isViewing) {
            //vm.steps.push("Confirmation");
        }
        vm.clear = function clear() {
            if (vm.isViewing)
                $uibModalInstance.close(true);
            else {
                $state.reload("operation-envoi-cash-to-account");
            }
        };


       function validation() {
     vm.traitementOperation();
     $uibModalInstance.close();
            
        }

        function voirCommentaire(commentaire) {
            console.log('comm',commentaire);
            //vm.commentaire=vm.operationEnCours.commentaire;
            $uibModal.open({
                templateUrl: 'app/entities/operation/account-cash/voir-commentaire.html',
                controller: 'VoirCommentaireController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return commentaire;
                    }
                }
            }).result.then(function () {
                $uibModalInstance.close(true);
            });
        }

        vm.motifs = [];


        vm.comptesB = {};

        vm.comptes = {};

        vm.beneficiaireInfos = {
            compteCBIDTO: {},
            clientDTO: {},
            personneDTO: {
                typePersonne: null
            },
            personneMoraleDTO: {},
            personnePhysiqueDTO: {}
        };

        vm.emetteurInfos = {
            compteCBIDTO: {},
            clientDTO: {},
            personneDTO: {},
            personneMoraleDTO: {},
            personnePhysiqueDTO: {}
        };


        $scope.fermerM= function () {
            $uibModalInstance.dismiss('cancel');
        };
        $scope.validerOp= function () {
          
          
            $uibModalInstance.close();

        };
        $scope.voirSolde = function () {
            $scope.ctrlSolde= true;
        };

        if (vm.operation.id != null) {
            console.log("init all");
            initUO(vm.operation.uoAcquereurId);
        }
//
        function initUO(uoId) {
            RequestUtils.GetUoPereByTypeOptimise.get({
                uoId: uoId,
                libelleTypeUO: "PAYS"
            }, function (datas) {
                vm.paysId = datas.id;
            });

            RequestUtils.GetUoPereByTypeOptimise.get({
                uoId: uoId,
                libelleTypeUO: "FSE"
            }, function (datas) {
                vm.fseId = datas.id;
            });

            UniteOrganisation.get({
                id: uoId
            }, function (datas) {
                vm.typeUOId = datas.typeUOId;
                TypeUniteOrganisation.get({
                    id: vm.typeUOId
                }, function (datas) {
                    vm.typeActeur = datas.libelleTypeUO;
                    if (vm.typeActeur == 'AGENCE') {
                        vm.agenceId = uoId;
                    }


                    if (vm.typeActeur == 'MASTER') {
                        RequestUtils.GetUoPereByTypeOptimise.get({
                            uoId: uoId,
                            libelleTypeUO: "AGENCE"
                        }, function (datas) {
                            vm.agenceId = datas.id;
                        });
                        vm.masterId = uoId;
                    }

                    if (vm.typeActeur == 'SOUS_DISTRIBUTEUR') {
                        RequestUtils.GetUoPereByTypeOptimise.get({
                            uoId: uoId,
                            libelleTypeUO: "AGENCE"
                        }, function (datas) {
                            vm.agenceId = datas.id;
                        });
                        RequestUtils.GetUoPereByTypeOptimise.get({
                            uoId: uoId,
                            libelleTypeUO: "MASTER"
                        }, function (datas) {
                            vm.masterId = datas.id;
                        });
                        vm.sousDistributeurId = uoId;
                    }

                });
            });
        }

        OperationUtils.initVerificationOrganisatioType(vm);


        OperationUtils.init($scope, vm, vm.typeService, vm.operation);

        vm.steps = [
            'Information',
            'Expéditeur',
            'Bénéficiaire',
            'Organisation Acquereur',
            'Organisation Emetteur'
        ];

        if (!vm.isViewing) {
            vm.steps.push("Confirmation");
        }

        var datas = {
            vm: vm,
            steps: vm.steps
        };


        InitSteps.initSteps(datas);



    }
})();
