(function () {
        'use strict';

        angular
            .module('ptiApp')
            .controller('RegulationController', RegulationController);

        RegulationController.$inject = ['$scope', '$state','$uibModal','Notification','Regulation','BASE_URL_API',  'RequestUtils', 'Utils','UniteOrganisation','TypeUniteOrganisation'];

        function RegulationController( $scope, $state, $uibModal,Notification,Regulation,BASE_URL_API, RequestUtils, Utils,UniteOrganisation, TypeUniteOrganisation) {

            var vm = this;
            var URL = BASE_URL_API + "/clients";
            vm.filtrerRechS=filtrerRechS;
            vm.getOperationS=getOperationS;
            vm.tabS=false;
            vm.tabD=false;
            vm.idOp=false;
            vm.idOp1=false;
            vm.f1=false;
            vm.f2=false;
            vm.cptSId=null;
            vm.test=null;
            vm.test2=null;
            vm.listCptS=listCptS;
            vm.actionOpS=actionOpS;
            vm.filtrerRechD=filtrerRechD;
            vm.getOperationD=getOperationD;
            vm.listCptD=listCptD;
            vm.actionOpD=actionOpD;
            vm.finishOp=finishOp;
            vm.cptUO=null;
            vm.annulerOp=annulerOp;
            vm.item = {
                typeOp:null,
                "nom":'',
                "telephone":'',
                "numeroCpt":'',
                typeC:null
            };
            vm.item1 = {
                typeOp:null,
                "nom":'',
                "telephone":'',
                "numeroCpt":'',
                typeC:null
            };
            vm.modal = {
                cptS:null,
                cptD:null,
                typeSolder:null,
                mntSolder:null,
                commentaire:null
            };
            vm.itemSearch = {
                'pereUOId':null,
                'libelleUniteOrganisation':'',
                'codeUO':'',
                'numeroOrdre':null,
                'typeUO':null,
                'pereUoId':null
            };
            vm.itemSearch1 = {
                'pereUOId':null,
                'libelleUniteOrganisation':'',
                'codeUO':'',
                'numeroOrdre':null,
                'typeUO':null,
                'pereUoId':null
            };
            vm.operations1 = [
                {
                    id:1,
                    libelle:'Clients entreprises',
                    url: 'api/clients',
                    type:'CLIENT',
                    params: {
                        ordi:'Client',
                        entrp: 'entreprise'
                    }

                },
                {
                    id:2,
                    libelle:'Clients particuliers',
                    url: 'api/clients',
                    type:'CLIENT',
                    params: {
                        ordi:'Client',
                        entrp: 'particulier'
                    }

                },
                {
                    id:3,
                    libelle:'Agents',
                    url: 'api/uo',
                    type:'AGENT',
                    params: {
                        ordi:'agent'
                    }

                }

            ];
            vm.operations2 = [
                {
                    id:1,
                    libelle:'Clients entreprise',
                    url: 'api/clients',
                    type:'CLIENT',
                    params: {
                        ordi:'Client',
                        entrp: 'entreprise'
                    }

                },
                {
                    id:2,
                    libelle:'Clients particuliers',
                    url: 'api/clients',
                    type:'CLIENT',
                    params: {
                        ordi:'Client',
                        entrp: 'particulier'
                    }

                },
                {
                    id:3,
                    libelle:'Agents',
                    url: 'api/uo',
                    type:'AGENT',
                    params: {
                        ordi:'agent'
                    }

                }

            ];
            vm.typeuniteorganisations =[] ;
            vm.FSEs=UniteOrganisation.queryByTypeUoFSE();
            TypeUniteOrganisation.queryOptimisetUo(function (typeUniteOrganisations) {
                let i=0;
                for(i=0;i<typeUniteOrganisations.length;i++){
                    if(typeUniteOrganisations[i].libelleTypeUO !="PAYS"
                        &&typeUniteOrganisations[i].libelleTypeUO !="FSE"
                        &&typeUniteOrganisations[i].libelleTypeUO !="ZONE"){
                        vm.typeuniteorganisations.push(typeUniteOrganisations[i]);
                    }
                }
            });
            function annulerOp() {
                vm.tabS=false;
                vm.tabD=false;
                vm.listOp=null;
                vm.listUO=null;
                vm.item = {
                    typeOp:null,
                    nom:'',
                    telephone:'',
                    numeroCpt:''
                };
                vm.itemSearch = {
                    'pereUOId':null,
                    'libelleUniteOrganisation':'',
                    'codeUO':'',
                    'numeroOrdre':null,
                    'typeUO':null,
                    'pereUoId':null
                };
                vm.item1 = {
                    typeOp:null,
                    nom:'',
                    telephone:'',
                    numeroCpt:''
                };
                vm.itemSearch1 = {
                    'pereUOId':null,
                    'libelleUniteOrganisation':'',
                    'codeUO':'',
                    'numeroOrdre':null,
                    'typeUO':null,
                    'pereUoId':null
                },
                  vm.nomS,vm.prenomS,vm.nomAS,vm.pereS, vm.raisonSocialeS =null;
                  vm.nomD=null,vm.prenomD=null,vm.nomAD=null,vm.pereD=null,vm.raisonSocialeD =null;
            }
            function filtrerRechS(id) {
                vm.etat = _.find(vm.operations2, {'id': id});
                vm.tabS=true;
                if (vm.idOp==='Client')
                {
                    if (vm.item.typeC==='particulier')
                    {

                        Regulation.queryAllClient({nom:vm.item.nom ,telephone:vm.item.telephone, numeroCpt:vm.item.numeroCpt}, function (respone) {
                            console.log(vm.item.nom);
                            console.log('clients',respone);
                            vm.listOp=respone;
                        });
                    } else {
                        Regulation.queryAllClientEntrp({nom:'%'+vm.item.nom+'%' ,telephone:'%'+vm.item.telephone+'%', numeroCpt:'%'+vm.item.numeroCpt+'%'},function (response) {
                            vm.listOp=response;
                            console.log('entrep',response);
                        })
                    }
                }
                if (vm.idOp==='agent') {
                   // console.log('ok',vm.itemSearch.typeUO,vm.itemSearch.codeUO,vm.itemSearch.pereUoId,vm.itemSearch.libelleUniteOrganisation);
                    Regulation.queryAllUO({libelleUniteOrganisation:vm.itemSearch.libelleUniteOrganisation
                        ,codeUO:vm.itemSearch.codeUO,typeUO:vm.itemSearch.typeUO,pereUoId:vm.itemSearch.pereUoId},function (response) {
                        console.log(response);
                        vm.listUO=response;
                    });

                }
            }
            function listCptS(uoId,nom,pere) {
                vm.nomS=null;
                vm.prenomS=null;
                vm.raisonSocialeS=null;
                vm.nomAS=nom;
                vm.pereS=pere;
                Regulation.getCompteByUO({uoId:uoId},function (data) {
                    console.log('liste des comptes',data);
                    vm.cptUO=data;
                    console.log('oo',vm.cptUO);
                    $uibModal.open({
                        templateUrl: 'app/entities/operation/regulation/compteS-modal.html',
                        controller: 'CompteModalController',
                        controllerAs: vm,
                        backdrop: 'static',
                        scope: $scope,
                        size: 'md',
                        resolve: {
                            entity: function () {
                                return vm.cptUO
                            },
                            isViewing:false,
                        }

                    }).result.then(function () {
                        console.log('win',vm.modal.cptS,'et',vm.cptSId);
                    }, function () {
                        console.log('uo_cpt',vm.modal.cptS);
                        vm.cptSId=vm.modal.cptS.split(',');
                        console.log('albarkaUo',vm.cptSId);
                        vm.cptsrc=vm.cptSId[0];
                        vm.cptIDSource=vm.cptSId[1];
                    });
                });

            }
            function getOperationS(id) {
                vm.etat = _.find(vm.operations1, {'id': id});
                console.log(id,vm.etat.params.ordi);
                vm.idOp=vm.etat.params.ordi;
                vm.item.typeC=vm.etat.params.entrp;
            }
            function actionOpS(cpt,nom,prenom,raisonSocial) {
                vm.nomAD=null;
                vm.pereD=null;
                vm.nomS=nom;
                vm.prenomS=prenom;
                vm.raisonSocialeS=raisonSocial;
                vm.nomAS=null;
                vm.pereS=null;
                console.log('z',nom,raisonSocial,prenom);
                console.log('cpt---',cpt);
                $uibModal.open({
                        templateUrl: 'app/entities/operation/regulation/compteS-modal.html',
                        controller: 'CompteModalController',
                        controllerAs: vm,
                        backdrop: 'static',
                        scope: $scope,
                        size: 'md',
                        resolve: {
                            entity: function () {
                                return cpt
                            },
                            isViewing:false,
                        }

                    }).result.then(function () {
                        console.log('error')
                    }, function () {
                    vm.cptSId=vm.modal.cptS.split(',');
                    console.log('albarka',vm.cptSId);
                    vm.cptsrc=vm.cptSId[0];
                    vm.cptIDSource=vm.cptSId[1];
                    });
            }
            function filtrerRechD(id) {
                vm.etat = _.find(vm.operations2, {'id': id});
                vm.tabD=true;
                if (vm.idOp1==='Client')
                {
                    if (vm.item1.typeC==='particulier')
                    {

                        Regulation.queryAllClient({nom:'%'+vm.item1.nom+'%' , telephone:'%'+vm.item1.telephone+'%', numeroCpt:'%'+vm.item1.numeroCpt+'%'}, function (response) {
                            console.log(vm.item1.nom);
                            console.log('clients',response);
                            vm.listOp1=response;
                        });
                    } else {

                        Regulation.queryAllClientEntrp({nom:'%'+vm.item1.nom+'%' , telephone:'%'+vm.item1.telephone+'%', numeroCpt:'%'+vm.item1.numeroCpt+'%'},function (response) {
                            vm.listOp1=response;
                            console.log('entrep',response);
                        })
                    }

                }
                if (vm.idOp1==='agent') {
                    console.log('ok',vm.itemSearch1.typeUO,vm.itemSearch1.codeUO,vm.itemSearch1.pereUoId,vm.itemSearch1.libelleUniteOrganisation);
                    Regulation.queryAllUO({libelleUniteOrganisation:vm.itemSearch1.libelleUniteOrganisation
                        ,codeUO:vm.itemSearch1.codeUO,typeUO:vm.itemSearch1.typeUO,pereUoId:vm.itemSearch1.pereUoId},function (response) {
                        console.log(response);
                        vm.listUO1=response;
                    });

                }
            }
            function listCptD(uoId,nom,pere) {
                vm.nomAD=nom;
                vm.pereD=pere;
                Regulation.getCompteByUO({uoId:uoId},function (data) {
                    console.log('liste des comptes dest',data);
                    vm.cptUO=data;
                    console.log('oo',vm.cptUO);
                    $uibModal.open({
                        templateUrl: 'app/entities/operation/regulation/compteD-modal.html',
                        controller: 'CompteModalController',
                        controllerAs: vm,
                        backdrop: 'static',
                        scope: $scope,
                        size: 'md',
                        resolve: {
                            entity: function () {
                                return vm.cptUO
                            },
                            isViewing:false,
                        }

                    }).result.then(function () {
                        console.log(vm.modal.cptD);
                    }, function () {
                        console.log('uo_cpt',vm.modal.cptD);
                        vm.cptDId=vm.modal.cptD.split(',');
                        console.log('albarkaUo',vm.cptDId);
                        vm.cptdest=vm.cptDId[0];
                        vm.cptIDDestination=vm.cptDId[1]
                    });
                });

            }
            function getOperationD(id) {
                vm.etat = _.find(vm.operations2, {'id': id});
                console.log(id,vm.etat.params.ordi);
                vm.idOp1=vm.etat.params.ordi;
                vm.item1.typeC=vm.etat.params.entrp;
            }
            function actionOpD(cpt,nom,prenom,raisonSocial) {
                vm.nomAD=null;
                vm.pereD=null;
                vm.nomD=nom;
                vm.prenomD=prenom;
                vm.raisonSocialeD=raisonSocial;
                console.log('zzzz--',nom,raisonSocial,prenom);
                console.log('cpt---',cpt);
                $uibModal.open({
                    templateUrl: 'app/entities/operation/regulation/compteD-modal.html',
                    controller: 'CompteModalController',
                    controllerAs: vm,
                    backdrop: 'static',
                    scope: $scope,
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return cpt
                        },
                        isViewing:false,
                    }

                }).result.then(function () {
                    console.log('error');
                }, function () {
                    console.log('test',vm.modal.cptD);
                    vm.cptDId=vm.modal.cptD.split(',');
                    console.log('albarka2',vm.cptDId);
                    vm.cptdest=vm.cptDId[0];
                    vm.cptIDDestination=vm.cptDId[1]
                });
            }
            function finishOp() {
                vm.final ={
                    source:vm.cptIDSource,
                    destination:vm.cptIDDestination,
                    cptsource:vm.cptsrc,
                    cptdestination:vm.cptdest
                };
                if (vm.cptsrc==='null' || vm.cptdest==='null')
                {
                    Notification.error("Veillez sélectionnez un compte!.");
                    console.log('2',vm.final.cptsource);
                }
                else{
                    if(vm.cptsrc===vm.cptdest)
                    {
                        Notification.error("Erreur comptes identiques!.");
                    }
                    else {
                    $uibModal.open({
                        templateUrl: 'app/entities/operation/regulation/operation-final-modal.html',
                        controller: 'CompteModalController',
                        controllerAs: vm,
                        backdrop: 'static',
                        scope: $scope,
                        size: 'md',
                        resolve: {
                            entity: function () {
                                return vm.final
                            },
                            isViewing:false,
                        }

                    }).result.then(function () {
                        console.log('comm',vm.modal.mntSolder,vm.modal.typeSolder,vm.modal.commentaire,vm.modal.motif);
                        console.log(vm.final);
                        console.log(vm.cptIDSource,vm.cptIDDestination);
                        Regulation.doOperation({compteSrc:vm.cptIDSource,compteDest:vm.cptIDDestination,
                                montant:vm.modal.mntSolder,typeService:vm.modal.typeSolder,commentaire:vm.modal.commentaire,motif:vm.modal.motif},
                            function (response) {
                               if(response) {
                                console.log(response);
                                  // $state.go('regulation');
                                   annulerOp();
                                   Notification.success("Opération validé avec succes!.");
                                   $state.go('operation',{}, {reload: true});

                               }
                            })
                    }, function () {
                        console.log('erreur');
                    });
                    }
                }
            }

        }
    }

)();
