(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementCompteController', ChargementCompteController);

    ChargementCompteController.$inject = ['Notification','$scope','$uibModal','Principal','ChargementCompte','LigneChargementCompte','Utils','Upload','GroupeProfilActeur','$timeout'];

    function ChargementCompteController(Notification,$scope,$uibModal,Principal,ChargementCompte,LigneChargementCompte,Utils,Upload,GroupeProfilActeur,$timeout,error) {

        var vm = this;
        vm.chargementComptes = [];
        vm.progressingBarAzeroCompte = progressingBarAzeroCompte;
        vm.lireFichierCompte = lireFichierCompte;
        vm.supprimerFichierCompte = supprimerFichierCompte;
        vm.click=false;
        vm.isPrint=false;
        vm.getChargementCompte = getChargementCompte;
        vm.validerChrgmtCompte = validerChrgmtCompte;
        vm.afficheFichier = false;
        vm.openModalCompte = openModalCompte;
        vm.imprimerRapportCompte = imprimerRapportCompte;
        vm.clearCompte= clearCompte;
        vm.ctrlbtnsave= true;
        vm.profilCpt = null;
        vm.format=null;
        vm.page = 1;
        vm.page2=1;
        vm.totalItems = 0;
        vm.totalItems2 = 0;
        vm.itemsPerPage=20;
        vm.queryCount=0;
        vm.queryCount2=0;
        vm.urlChrgCpt='api/etat/chargement-compte/etat/rapportChargement';
        vm.groupeProfils=GroupeProfilActeur.queryByType({acteur:'CLIENT'});
        loadAll();
        function clearCompte (picFile){
            picFile = null;
            vm.progressionBar = 0;
            vm.fileName="";
            vm.ctrlbtnsave= true;
            vm.profilCpt= null;
        }
        function loadAll() {
            vm.afficheFichier = true;
            ChargementCompte.query({
                page:vm.page2-1,
                size:vm.itemsPerPage
            },function(result,headers) {
                vm.totalItems2=headers('X-Total-Count');
                vm.queryCount2 = result.length;
                vm.canShowResultCount2=true;
                vm.chargementComptes = result;
                vm.searchQuery = null;
            });
        }
        vm.selectedRow=null;
        function progressingBarAzeroCompte(file) {
            vm.progressionBar = 0;
            vm.fileName = file.name;
            vm.fil=file;
            vm.ctrlbtnsave= false;
        }
        function supprimerFichierCompte() {
            var charg = "";
            if (confirm('Voulez vous supprimer ce fichier')) {
                LigneChargementCompte.findByIdCpte({id: vm.defchargement.id}, function (data) {
                    data = angular.fromJson(data);
                    console.log(data.values);
                    vm.defchargement = charg;
                    loadAll();
                    getChargementCompte(charg, index);
                });
            }
        }
        function getChargementCompte(chargement,index) {
            vm.click = false;
            vm.defchargement = chargement;
            console.log(vm.defchargement);
            vm.selectedRow = index;
            LigneChargementCompte.queryDetailCpt({
                id:chargement.id,
                page:vm.page-1,
                size:vm.itemsPerPage
            }, function (result,headers) {
                console.log(result);
                vm.totalItems=headers('X-Total-Count');
                vm.queryCount = result.length;
                console.log('transaction',result);
                vm.canShowResultCount=true;
                vm.ligneChargements = result;
            });
        }
        $scope.$watch("vm.page", function (id) {
            getChargementCompte(vm.defchargement,vm.selectedRow);
        });
        $scope.$watch("vm.page2", function (id) {
            loadAll();
        });


        vm.selectedRow=null;
        vm.item ={
            idChargement:null,
            format:null
        };
        function imprimerRapportCompte(idChargement) {
            // var typeExel="application/vnd.ms-excel;base64";
            // var typepdf="application/pdf;base64";
            // ChargementCompte.imprimerCpte({codeChargement:idChargement},function (result){
            //     var pdf = new Blob([result.data], {type:  typepdf});
            //     var url = window.URL.createObjectURL(pdf);
            //     window.open(url);
            // })
            vm.item.idChargement=idChargement;
            console.log('t',vm.urlChrgCpt);
            console.log('format',vm.format);
            if(vm.format===null || vm.format===''||vm.format==='undefined'){
                console.log('f',vm.format);
                vm.item.format = 'PDF';
            }else {
                vm.item.format=vm.format;
            }
            if(vm.item.format ==='PDF' || vm.item.format ===null)
            {
                Utils.postPrintUrl(vm.urlChrgCpt,vm.item);
            } else
            {
                if (vm.item.format === 'XLSX') {
                    vm.name = 'ChargementCompteMasse.xlsx';
                }
                if (vm.item.format === 'CSV') {
                    vm.name = 'ChargementCompteMasse.csv';
                }
                Utils.downloadFile(vm.urlChrgCpt,vm.item,vm.name);
            }

        }


function validerChrgmtCompte(chargement) {
            vm.click = true;
            console.log(vm.profilCpt);
            vm.operation=chargement;
            Principal.identity().then(function (account) {
                console.log('true',account);
                vm.operation.login = account.login;
            });
            $uibModal.open({
                templateUrl: 'app/entities/operation/reglement-organisation-validation/reglement-traitement.html',
                controller: 'ChargementCompteDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return vm.operation;
                    }
                }
            }).result.then(function () {
                //$uibModalInstance.close(true);
                loadAll();
                getChargementCompte(vm.defchargement,vm.selectedRow);

            });
                /*Principal.identity().then(function (account) {
                    vm.compte = account;
                    ChargementCompte.valider({
                        id: chargement.id,
                        user: account.login,
                        canal: 'WEB'
                    }, function (result, headers) {
                        var message = headers('X-ptiApp-params');
                        Notification.success(message);

                        loadAll();
                    });
                });*/
        }

        
        function openModalCompte(ligneChargement) {
            $uibModal.open({
                templateUrl: 'app/entities/ecran/ecran-detail.html',
                controller: 'EcranDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return  ligneChargement;
                    }
                }
            });

        }

        function lireFichierCompte(file) {
            console.log(vm.profilCpt);
            if(vm.profilCpt!=null) {
            var url ='api/upload/chargement_compte_client/fichier';
            Upload.upload({
                url: url,
                data: {file: file,profilId:vm.profilCpt}
            }).then(function(resp){
                console.log('result---------',resp);
                loadAll();
                if(resp.status === 200){
                    console.log(file);
                    $timeout(function () {
                        Notification.success('Fichier chargé sur le système avec succès', 'Succès');
                    }, 200);

                }
                else{
                    console.log('mal  passer');
                    var message = resp.header('X-partnerApp-alert');
                    Notification.success(message, 'succès');
                }
            }, function (resp) {
                console.log(resp);
               // var message = resp.header('X-partnerApp-alert');
                Notification.error('erreur de chargement du fichier\n echec encor\n');

            }, function (evt) {
                var progressPercentageCompte = parseInt(100.0 * evt.loaded / evt.total);
                vm.progressionBar = progressPercentageCompte;
            })

        } else {
                Notification.error('Veillez choisir un profil pour les comptes');
            }
        }
    }
})();
